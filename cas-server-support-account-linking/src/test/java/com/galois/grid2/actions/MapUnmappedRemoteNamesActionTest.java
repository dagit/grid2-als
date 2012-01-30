package com.galois.grid2.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;
import org.springframework.webflow.execution.ActionExecutionException;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import com.galois.grid2.GenericRemoteName;
import com.galois.grid2.LabeledField;
import com.galois.grid2.MutableAccountLinkingStorage;
import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteName;
import com.galois.grid2.UnmappedRemoteNameException;
import com.galois.grid2.actions.AuthenticationStore.AuthenticationHandle;
import com.galois.grid2.actions.MapUnmappedRemoteNamesAction.AttributeModel;
import com.galois.grid2.actions.MapUnmappedRemoteNamesAction.RemoteNameHandlingException;
import com.galois.grid2.store.MemoryAccountLinkingStorage;

public class MapUnmappedRemoteNamesActionTest extends TestCase {
	private static final class UpdateTrackingAuthenticationHandle implements
			AuthenticationHandle {
		boolean didUpdate = false;

		public String getLocalName() {
			return "a-name";
		}

		public void updateAttributes() {
			didUpdate = true;
		}

		public boolean updateCalled() {
			return didUpdate;
		}

		public void reset() {
			didUpdate = false;
		}
	}

	static final String EXC_KEY = TransitionExecutingFlowExecutionExceptionHandler.FLOW_EXECUTION_EXCEPTION_ATTRIBUTE;

	public void testLinkAccounts1() {
		final UpdateTrackingAuthenticationHandle handle = new UpdateTrackingAuthenticationHandle();
		AuthenticationStore authns = new AuthenticationStore() {
			public UpdateTrackingAuthenticationHandle getAuthenticationHandle(
					RequestContext context) {
				return handle;
			}
		};
		MutableAccountLinkingStorage storage = new MemoryAccountLinkingStorage();
		MapUnmappedRemoteNamesAction murna = new MapUnmappedRemoteNamesAction(
				storage, authns);
		RequestContext context = new MockRequestContext();
		murna.linkAccounts(context);

		Namespaced<RemoteName> name = mkName("ns",
				Collections.<String, String> singletonMap("key", "value"));
		storeException(context.getFlashScope(), name);

		murna.storeUnmappedName(context);
		murna.linkAccounts(context);
		assertTrue(handle.updateCalled());

		murna.linkAccounts(context);
		assertTrue(handle.updateCalled());

		handle.reset();
		murna.linkAccounts(context);
		assertFalse(handle.updateCalled());
	}

	public void testLinkAccounts2() {
		MutableAccountLinkingStorage storage = new MemoryAccountLinkingStorage();
		MapUnmappedRemoteNamesAction murna = new MapUnmappedRemoteNamesAction(
				storage, null);
		boolean result;
		String localName = "a-user";
		UnmappedRemoteNames unmappedNames = new UnmappedRemoteNames();

		result = murna.linkAccounts(localName, unmappedNames);
		assertFalse(result); // No linkages were created

		Namespaced<RemoteName> name1 = mkName("ns",
				Collections.<String, String> singletonMap("foo", "bar"));
		unmappedNames.add(name1);

		result = murna.linkAccounts(localName, unmappedNames);
		assertTrue(result); // A linkage was created

		result = murna.linkAccounts(localName, unmappedNames);
		assertFalse(result); // No linkage was created

		result = murna.linkAccounts("another-user", unmappedNames);
		assertFalse(result); // No linkage was created
	}

	public void testStoreUnmappedName() throws IllegalStateException,
			RemoteNameHandlingException {
		String result;
		UnmappedRemoteNames names;

		MapUnmappedRemoteNamesAction murna = new MapUnmappedRemoteNamesAction(
				null, null);
		RequestContext context = new MockRequestContext();

		result = murna.storeUnmappedName(context);
		assertEquals("error", result);

		Namespaced<RemoteName> name = mkName("ns",
				Collections.<String, String> singletonMap("key", "value"));
		storeException(context.getFlashScope(), name);

		result = murna.storeUnmappedName(context);
		assertEquals("new-name", result);
		names = murna.getUnmappedNames(context.getFlowScope());
		assertTrue(names.contains(name));

		result = murna.storeUnmappedName(context);
		assertEquals("already-tried", result);
		names = murna.getUnmappedNames(context.getFlowScope());
		assertTrue(names.contains(name));

	}

	private static Namespaced<RemoteName> mkName(String ns,
			Map<String, String> attrs) {
		return new Namespaced<RemoteName>(ns, new GenericRemoteName(attrs));
	}

	public void testAddUnmappedRemoteName() throws RemoteNameHandlingException {
		// Note that this method does not access instance state. It would be
		// static, but that would prevent the tests from mocking it.
		MapUnmappedRemoteNamesAction murna = new MapUnmappedRemoteNamesAction(
				null, null);
		MutableAttributeMap flowScope;
		UnmappedRemoteNames result;
		boolean isNew;

		flowScope = new LocalAttributeMap();
		Namespaced<RemoteName> remoteName = mkName("ns",
				Collections.<String, String> emptyMap());

		result = murna.getUnmappedNames(flowScope);
		assertFalse(result.contains(remoteName));

		isNew = murna.addUnmappedRemoteName(remoteName, flowScope);
		assertTrue(isNew);

		result = murna.getUnmappedNames(flowScope);
		assertTrue(result.contains(remoteName));

		isNew = murna.addUnmappedRemoteName(remoteName, flowScope);
		assertFalse(isNew);

		result = murna.getUnmappedNames(flowScope);
		assertTrue(result.contains(remoteName));
	}

	public void testGetUnmappedRemoteName() throws RemoteNameHandlingException {
		// Note that this method does not access instance state. It would be
		// static, but that would prevent the tests from mocking it.
		MapUnmappedRemoteNamesAction murna = new MapUnmappedRemoteNamesAction(
				null, null);

		MutableAttributeMap flashScope;

		flashScope = new LocalAttributeMap();
		try {
			murna.getUnmappedRemoteName(flashScope);
		} catch (RemoteNameHandlingException e) {
			assertNull(e.getCause());
		}

		flashScope = new LocalAttributeMap();
		flashScope.put(EXC_KEY, "Bad type");
		try {
			murna.getUnmappedRemoteName(flashScope);
		} catch (RemoteNameHandlingException e) {
			assertTrue("Expected class cast exception. Got:" + e.toString(),
					e.getCause() instanceof ClassCastException);
		}

		flashScope = new LocalAttributeMap();
		flashScope.put(EXC_KEY, new ActionExecutionException("bogus", "bogus",
				null, null, null));
		try {
			murna.getUnmappedRemoteName(flashScope);
		} catch (RemoteNameHandlingException e) {
			assertNull(e.getCause());
		}

		flashScope = new LocalAttributeMap();
		flashScope.put(EXC_KEY, new ActionExecutionException("bogus", "bogus",
				null, null, new RuntimeException()));
		try {
			murna.getUnmappedRemoteName(flashScope);
		} catch (RemoteNameHandlingException e) {
			assertTrue("Expected class cast exception. Got:" + e.getCause(),
					e.getCause() instanceof ClassCastException);
		}

		flashScope = new LocalAttributeMap();
		final Namespaced<RemoteName> expected = mkName("ns",
				Collections.<String, String> emptyMap());
		storeException(flashScope, expected);
		assertEquals(murna.getUnmappedRemoteName(flashScope), expected);
	}

	private void storeException(MutableAttributeMap flashScope,
			Namespaced<RemoteName> remoteName) {
		flashScope.put(EXC_KEY, new ActionExecutionException("bogus", "bogus",
				null, null, new UnmappedRemoteNameException(remoteName)));
	}

	public void testGetUnmappedNames() throws RemoteNameHandlingException {
		// Note that this method does not access instance state. It would be
		// static, but that would prevent the tests from mocking it.
		MapUnmappedRemoteNamesAction murna = new MapUnmappedRemoteNamesAction(
				null, null);
		MutableAttributeMap flowScope;
		UnmappedRemoteNames result;

		flowScope = new LocalAttributeMap();
		result = murna.getUnmappedNames(flowScope);
		assertTrue(result.isEmpty());

		flowScope = new LocalAttributeMap();
		flowScope.put(MapUnmappedRemoteNamesAction.UNMAPPED_REMOTE_NAMES,
				"bad type");
		try {
			result = murna.getUnmappedNames(flowScope);
			fail(result.toString());
		} catch (RemoteNameHandlingException e) {
			assertTrue(e.toString(), e.getCause() instanceof ClassCastException);
		}
	}

	public void testGetModel() throws RemoteNameHandlingException {
		MapUnmappedRemoteNamesAction murna = new MapUnmappedRemoteNamesAction(
				null, null);
		MutableAttributeMap flowScope;
		flowScope = new LocalAttributeMap();
		List<AttributeModel> model;
		List<AttributeModel> expected;

		model = murna.getModel(flowScope);
		assertEquals(Collections.<AttributeModel> emptyList(), model);

		final Namespaced<RemoteName> expectedName = mkName("ns",
				Collections.<String, String> singletonMap("label", "value"));
		murna.addUnmappedRemoteName(expectedName, flowScope);

		model = murna.getModel(flowScope);

		expected = new ArrayList<AttributeModel>();

		AttributeModel modelData = new AttributeModel(
				Collections.singletonList(new LabeledField("label", "value")),
				Collections.<LabeledField> emptyList());
		expected.add(modelData);
		assertEquals(expected, model);
	}
}
