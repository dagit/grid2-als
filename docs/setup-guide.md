Account-Linking Setup Guide
===========================

This guide provides step-by-step instructions for installation and
configuration of the Central Authentiation Service (CAS) and a
protected resource which performs authorization based on its
interactions with CAS.  Our extended version of CAS provides
account-linking capability, which permits users with
externally-provisioned credentials to gain access to local resources
based on localy-meaningful access control policies.

This guide assumes that the deployment environment is CentOS 5.  It
also assumes that the system is not already running any of the
services configured by these instructions.  Feel free to skip steps
for components which you already have, provided you know the relevant
details (paths, configuration files, etc.).

By following these instructions you will:

 * Install and configure CAS to provides account linking support
   between different sets of credentials. In particular, between
   credentials stored in LDAP and X.509 client certificates registered
   in VOMS;

 * Configure an example of a protected resource which performs
   authorization based solely on attributes and account metadata in
   the "local" attribute namespace, while allowing access for users
   with non-local credentials and attributes

Setting up the Identity Provider
================================

This guide first goes through the setup and configuration of an
idenity provider ("IDP"). We assume that LDAP, VOMS, or any other
source of attributes already exist and are available.  Note that any
environment variables exported may need to be added to /etc/profile.

What you'll need:

 * A clean installation of CentOS 5
 * The Galois-provided setup archive, idp.tar.gz.
 * SELinux disabled or in permissive mode
 * 'root' access on the target host
 * Local CA X.509 certificate (we'll refer to this at "cacert.pem")
 * SSL certificate and key for Apache issued by your local CA (we'll
   refer to these as httpd_cert.pem and httpd_key.pem)
 * Local LDAP bind permission
 * Client certificate, key, and key passphrase, issued by your local
   CA, for CAS to use when fetching VOMS attributes (we'll refer to
   these as 'client_cert.pem' and 'client_key.pem')
 * Network access to the Galois Maven repository for other artifacts
   (located at http://maven.grid2.galois.com/)

Identity Provider Installation
------------------------------

 * Log in as 'root'.

 * Install required packages:
   yum install httpd httpd-devel mod_ssl mysql mysql-server gcc gcc-c++ make autoconf

   Alternatively for CentOS 5:
   yum groupinstall "Development Tools" "Development Libraries"
   yum groupinstall "Web Server" "MySQL Server"

 * Unpack idp.tar.gz.  Below, files that come from idp.tar.gz are prefixed with
   "idp" in the path.
   tar xvzf idp.tar.gz

   # We'll be referring to this directory in the rest of the
   # instructions.  We'll also assume that $HERE is the location of
   # your certificate files.
   export HERE=`pwd`

 * Install your CA certificate (here named "cacert.pem"):
   export HASH=$(openssl x509 -hash -in $HERE/cacert.pem -noout)
   install -D cacert.pem /etc/grid-security/certificates/$HASH.0 -m 0644

 * Install the Oracle Java SE JDK version 7:
   - Navigate to:
       http://www.oracle.com/technetwork/java/javase/downloads/java-se-jdk-7-download-432154.html
   - Download either "Linux x86 - RPM Installer" or "Linux x64 - RPM
     Installer" depending on your architecture

   rpm -ivh $HERE/jdk-7-linux-(arch).rpm
   export JAVA_HOME=/usr/java/latest/

 * Maven
   wget http://apache.osuosl.org//maven/binaries/apache-maven-2.2.1-bin.tar.gz
   mkdir -p /usr/local/maven
   pushd /usr/local/maven
   tar -xvf $HERE/apache-maven-2.2.1-bin.tar.gz
   popd
   export M2_HOME=/usr/local/maven/apache-maven-2.2.1/
   Add to PATH: $M2\_HOME/bin

 * Tomcat
   /usr/sbin/adduser tomcat -r
   pushd /opt
   wget http://apache.osuosl.org/tomcat/tomcat-5/v5.5.34/bin/apache-tomcat-5.5.34.tar.gz
   tar xvzf apache-tomcat-5.5.34.tar.gz

   # Note: this has a security consequence, which is that Tomcat can
   # write to its own binaries.  We need an alternative permissions
   # layout.
   chown -R tomcat:tomcat apache-tomcat-5.5.34
   install -m 0555 $HERE/idp/tomcat /etc/init.d/
   popd
   /sbin/chkconfig --level 345 tomcat on

 * Apache-tomcat connector
   wget http://apache.osuosl.org//tomcat/tomcat-connectors/jk/tomcat-connectors-1.2.32-src.tar.gz
   pushd /opt/apache-tomcat-5.5.34
   tar xvzf $HERE/tomcat-connectors-1.2.32-src.tar.gz
   pushd tomcat-connectors-1.2.32-src/native
   ./configure --with-apxs=/usr/sbin/apxs
   make
   make install
   popd

 * Apache
   /sbin/chkconfig --level 345 httpd on

 * MySQL
   /sbin/chkconfig --level 345 mysqld on
   /sbin/service mysqld start

Identity Provider Configuration
-------------------------------

 * Apache

   - Install host cert and key:
     mkdir -p /etc/grid-security/http
     install -D $HERE/httpd_key.pem /etc/grid-security/http/$(hostname)-http_key.pem -m 0400 -o apache -g apache
     install -D $HERE/httpd_cert.pem /etc/grid-security/http/$(hostname)-http_cert.pem -m 0400 -o apache -g apache

   - Configure apache to use a host certificate and key.  In
     /etc/httpd/conf.d/ssl.conf, modify these directives with these
     values, you will need to substitute $(hostname) with the actual
     hostname:

     SSLCertificateFile /etc/grid-security/http/$(hostname)-http_cert.pem
     SSLCertificateKeyFile /etc/grid-security/http/$(hostname)-http_key.pem
     SSLCACertificatePath /etc/grid-security/certificates

   - mod_jk configuration:; Create /etc/httpd/conf.d/mod_jk.conf with
     this content:

     LoadModule jk_module modules/mod_jk.so
     JkLogFile logs/mod_jk.log
     JkLogLevel info
     # "JkMountCopy all" sends JkMount definitions to all virtual hosts
     JkMountCopy all
     JkOptions +ForwardDirectories

     JkWorkerProperty worker.list=tomcat
     JkWorkerProperty worker.tomcat.type=ajp13
     JkWorkerProperty worker.tomcat.host=localhost
     JkWorkerProperty worker.tomcat.port=8009

     JkMount /cas/* tomcat
     <Location /cas>
         SSLOptions +StdEnvVars +ExportCertData
         SSLVerifyClient optional
     </Location>

 * MySQL
   - Creation of account and database for CAS account linking
   Note: The following is "outdented" so that copying and pasting won't
   break the heredoc.
mysql -u root <<EOF
CREATE USER account_linking;
CREATE DATABASE cas_account_linking;
GRANT ALL PRIVILEGES ON cas_account_linking.* TO 'account_linking'@'localhost';
EOF

 * Tomcat
   - Install trust store with client cert and key for CAS to use when
     connecting to voms-admin
   export KEYTOOL="/usr/java/latest/bin/keytool"
   export KEYSTORE=/opt/apache-tomcat-5.5.34/conf/account_linking.jks

   - Create a PKCS12 store from the existing client certificate and
     private key.

     * If you run this command as non-root but you ran other openssl
       commands as root, you may get the error "unable to write
       'random state'".  If you do, delete ~/.rnd and try again.

     * You must set an export password, otherwise the next step will
       fail with a "/ by zero" exception.

   openssl pkcs12 -export \
       -in "$HERE/client_cert.pem" \
       -inkey "$HERE/client_key.pem" \
       -out "cas_client.p12"

   - Create Java key store using PKCS12 as input
   $KEYTOOL -importkeystore -destkeystore $KEYSTORE -srckeystore cas_client.p12 \
       -srcstoretype PKCS12

   - Import the CA certificate (answer 'yes' when asked to trust):
   $KEYTOOL -importcert -keystore $KEYSTORE -file $HERE/cacert.pem -alias "grid2"

   - Set the correct owner and permissions on the trustStore so that tomcat can use it
   chmod 440 $KEYSTORE
   chown tomcat:tomcat $KEYSTORE

   - Update tomcat's setenv.sh to use the key store
   Note: The following is "outdented" so that copying and pasting won't
   break the heredoc.
export SETENV_TMP="$(mktemp)"
cat <<EOF > "$SETENV_TMP"
JAVA_OPTS=-Djavax.net.ssl.trustStore=$KEYSTORE
EOF

   install \
     --backup=numbered \
     --group=tomcat \
     --owner=tomcat \
     --mode=555 \
     "$SETENV_TMP" \
     "/opt/apache-tomcat-5.5.34/bin/setenv.sh"

 * Start Tomcat
   /sbin/service tomcat start

 * Start httpd
   /sbin/service httpd start

 * Configure CAS WAR overlay
   - Unpack CAS WAR overlay package
   pushd idp/cas-config

   - Modify
     src/main/webapp/WEB-INF/spring-configuration/account-linking.xml
     as appropriate; for details on how to configure CAS account
     linking, please see cas-als-configuration.txt.  In paricular, you
     will need to change the keystore settings in the
     "com.galois.grid2.voms.SOAPAttributeFetcher" bean definition to
     match the "cas_client.p12" store which you generated above.  The
     key passphrase and key store passphrase must be set correctly.
     See the following properties of the bean definition:

      * keystorePassword - the PKCS12 passphrase
      * keypass - the key passphrase
      * keystore - the value of $KEYSTORE

     In addition, you will need to configure:

      * The "url" property of the "grid2IPALDAP" bean definition in
        deployerConfigContext.xml to point to your LDAP server. We recommend
        using "ldaps" URLs so that the connection uses SSL/TLS.

      * The "trustedIssuerDnPattern" property of the
        "X509CredentialsAuthenticationHandler" bean definition in
        deployerConfigContext.xml; this is a regular expression which
        is used to match the DNs of X.509 client certificates used to
        authenticate to CAS.

      * The "filter" property of the
        "FastBindLdapAuthenticationHandler" bean definition in
        deployerConfigContext.xml; this is a pattern which forms the
        LDAP DN of user accounts in your LDAP server.  Note the
        presence of "%u" to indicate the location of the username in
        the DN string.

      * The base DN strings and patterns in the attributes of the
        various "pipeline" and "merge" elements of
        account-linking.xml.  These strings should be changed to
        reflect the structure of your user account DNs and location of
        your user accounts in LDAP.

      * The "nameOut" attribute of the "memberQueryDao" bean
        definition in account-linking.xml; the value, which defaults
        to "member", should be the name of the LDAP attribute used for
        group members in your LDAP schema.

   - Use Maven to build CAS WAR file.  NOTE: This step takes a long
     time and will download a lot of packages.  This step downloads
     java resources from public Java Maven repositories which are
     required for CAS and its dependencies.  However, once downloaded,
     these packages will be cached in ~/.m2/.

     mvn package
     popd

 * Install CAS WAR
   - Copy WAR file to appropriate location

   install -o tomcat -g tomcat -m 0444 idp/cas-config/target/cas.war /opt/apache-tomcat-5.5.34/webapps/

 * Test CAS
   - Visit CAS in a web browser to check that it is running

   - Run the included "try-cas-login" program to fetch attributes from
   a CAS authentication:

     * Edit try-cas-login.ini to match local environment.  You'll need
       to adjust the following settings to match your configuration:

       - cas-login-url - should be set to https://(HOSTNAME)/cas/login
       - cas-saml-url - should be set to https://(HOSTNAME)/cas/samlValidate
       - username - the username in your LDAP server which you want to
         use to test
       - key - should be set to $HERE/client_key.pem
       - cert - should be set to $HERE/client_cert.pem
       - ca-file - should be set to $HERE/cacert.pem

     * Run try-cas-login:
       pushd $HERE/idp/try-cas-login
       ./try-cas-login username --attributes-only

Setting up the Relying Party
============================

This guide goes through the setup and configuration of a relying party
("rp").  The provided rp should be viewed as a simple example of how
to use mod\_auth\_cas, as such the provided rp simply dumps the CGI
environment of the currently authenticated user.

What you'll need:

 * 'root' access on the relying party host
 * Network access
 * The Galois-provided setup files in rp.tar.gz.
 * The following installed software:
   * Apache httpd with development headers
   * gcc
   * GNU make
   * libcurl with development headers
   * OpenSSL with development headers

In general, we recommend using SSL for the relying party's web server.
Our example below uses unencrypted HTTP.

Relying Party Installation
--------------------------

 * Unpack rp.tar.gz.  Below, files that come from rp.tar.gz are
   prefixed with "rp" in the path.

   cd $HERE
   tar xvzf $HERE/rp.tar.gz

 * Install Apache

   yum install gcc httpd httpd-devel openssl-devel curl-devel

 * Install mod\_auth\_cas

   pushd $HERE/rp/mod_auth_cas
   ./configure --with-apxs=/usr/sbin/apxs
   make
   make install
   popd

 * Install the example config file for mod_auth_cas:

   install -m 0644 -o apache -g apache $HERE/rp/mod_auth_cas.conf /etc/httpd/conf.d/mod_auth_cas.conf

 * Install an example relying party; this one is a simple cgi script
   that lists the attributes it received from CAS.

   install -D -m 0544 -o apache -g apache $HERE/rp/env /var/www/cgi-bin/env
   install -D -m 0544 -o apache -g apache $HERE/rp/env-protected /var/www/cgi-bin/env-protected
   install -D -m 0644 -o apache -g apache $HERE/rp/index.html /var/www/html/index.html

 * mod_auth_cas needs a place to put its cookies.  Note that on some
   distributions, /var/run/ is transient like /tmp, so you may need to
   choose a different path which persists across reboots.  If you
   change this path, make sure it is reflected in the mod_auth_cas
   configuration.

   install -d -m 0700 -o apache -g apache /var/run/mod_auth_cas

 * Restart now that we've changed the config
   /sbin/chkconfig --level 345 httpd on
   /sbin/service httpd restart

Relying Party Configuration
---------------------------

 * Modify /etc/httpd/conf.d/mod_auth_cas.conf to Configure
   mod\_auth\_cas.  See the README document in $HERE/rp/mod_auth_cas/.
   In particular, the section entitled "CONFIGURING THE SOFTWARE"
   details using the Apache directives provided by mod_auth_cas to
   protect content based on CAS attributes.  The included
   mod_auth_cas.conf provides a basic example configuration.

 * Test the relying party configuration; for more information, the
   CASDebug directive may be set to "On" to enable mod_auth_cas debug
   log messages.

    - Access the protected resource you configured above using your
      web browser.  You should see a listing of the CGI environment
      variables along with any and all attributes released by CAS,
      prefixed with the prefix chosen in the mod_auth_cas
      configuration.

    - If you need to troubleshoot, here are some things which can go
      wrong at this point:

      - mod_auth_cas uses CURL to communicate with CAS, and CURL must
        have access to the CA certificate which issued the CAS service
        certificate.  By default that certificate must be part of the
        CA certificate bundle in /etc/pki/tls/certs/ca-bundle.crt.

      - The RP host firewall, if any, may block incoming traffic to
        Apache.

      - On a 64-bit system, Apache may fail to start because it cannot
        find /usr/lib/libcurl.so. The mod_auth_cas.conf file should be
        changed to point to /usr/lib64/libcurl.so.

      - The RP may not be able to reach the CAS server.
