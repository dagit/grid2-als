<jsp:directive.include file="../default/ui/includes/top.jsp" />
<form:form method="post" id="fm1" cssClass="fm-v clearfix"
	commandName="${commandName}" htmlEscape="true">
	<div id="login" class="box fl-panel account-linking">
		<h2>Account Linking Needed</h2>
		<p>You have successfully authenticated as:</p>
		<table class="account-linking-summary">
			<c:forEach var="remoteName" items="${unmappedNames}">
				<tr>
					<td>
						<table>
							<c:forEach var="field" items="${remoteName.userAttributes}">
								<tr>
									<th>${field.label}:</th>
									<td>${field.value}</td>
								</tr>
							</c:forEach>
						</table>
					</td>
					<td>
						<table>
							<c:forEach var="field" items="${remoteName.authorityAttributes}">
								<tr>
									<th>${field.label}:</th>
									<td>${field.value}</td>
								</tr>
							</c:forEach>
						</table>
					</td>
				</tr>
			</c:forEach>
		</table>
		<p>That authentication must be linked to a local account to
			continue.</p>

		<input type="hidden" name="execution" value="${flowExecutionKey}" />
		<input type="hidden" name="_eventId" value="restart-login" /> <input
			class="btn-submit" name="submit" accesskey="l"
			value="Link to a local account" tabindex="4" type="submit" />
		<p>
			<a href="${localAccountProvisioningURL}">I can't access a local
				account.</a>
		</p>
	</div>
</form:form>
<jsp:directive.include file="../default/ui/includes/bottom.jsp" />