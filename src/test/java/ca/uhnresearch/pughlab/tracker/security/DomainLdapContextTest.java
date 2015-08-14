package ca.uhnresearch.pughlab.tracker.security;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.anyObject;

public class DomainLdapContextTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testConstructor() {
		DomainLdapContext context = new DomainLdapContext();
		Assert.assertNotNull(context);
	}

	@Test
	public void testGetSetLdapHost() {
		DomainLdapContext context = new DomainLdapContext();
		context.setLdapHost("ad.example.com");
		Assert.assertEquals("ad.example.com", context.getLdapHost());
	}

	@Test
	public void testGetSetDomain() {
		DomainLdapContext context = new DomainLdapContext();
		context.setDomain("example.com");
		Assert.assertEquals("example.com", context.getDomain());
	}

	@Test
	public void testGetSetLdapPort() {
		DomainLdapContext context = new DomainLdapContext();
		context.setLdapPort(8389);
		Assert.assertEquals(8389, context.getLdapPort());
	}

	@Test
	public void testGetSetTimeout() {
		DomainLdapContext context = new DomainLdapContext();
		context.setTimeout(120);
		Assert.assertEquals(120, context.getTimeout());
	}

	@Test
	public void testGetSetSearchTemplate() {
		DomainLdapContext context = new DomainLdapContext();
		context.setSearchTemplate("OU=People,DC=example,DC=com");
		Assert.assertEquals("OU=People,DC=example,DC=com", context.getSearchTemplate());
	}

	@Test
	public void testGetSetFilterTemplate() {
		DomainLdapContext context = new DomainLdapContext();
		context.setFilterTemplate("(userPrincipalName={0})");
		Assert.assertEquals("(userPrincipalName={0})", context.getFilterTemplate());
	}

	@Test
	public void testGetSetMinEvictableIdleTimeMillis() {
		DomainLdapContext context = new DomainLdapContext();
		context.setMinEvictableIdleTimeMillis(12345);
		Assert.assertEquals(12345, context.getMinEvictableIdleTimeMillis());
	}

	@Test
	public void testGetSetTimeBetweenEvictionRunsMillis() {
		DomainLdapContext context = new DomainLdapContext();
		context.setTimeBetweenEvictionRunsMillis(54321);
		Assert.assertEquals(54321, context.getTimeBetweenEvictionRunsMillis());
	}
	
	@Test 
	public void testQuerySuccess() throws Exception {
		
		Realm realm = createMock(Realm.class);
		expect(realm.getName()).andStubReturn("mockrealm");
		replay(realm);
		
		LdapResult bindResult = createMock(LdapResult.class);
		expect(bindResult.getResultCode()).andStubReturn(ResultCodeEnum.SUCCESS);
		replay(bindResult);
		
		BindResponse bindResponse = createMock(BindResponse.class);
		expect(bindResponse.getLdapResult()).andStubReturn(bindResult);
		replay(bindResponse);
		
		Entry resultEntry = createMock(Entry.class);
		replay(resultEntry);
		
		EntryCursor cursor = createMock(EntryCursor.class);
		expect(cursor.next()).andReturn(true);
		expect(cursor.get()).andReturn(resultEntry);
		replay(cursor);
		
		LdapConnection connection = createMock(LdapConnection.class);
		expect(connection.bind(anyObject(BindRequest.class))).andStubReturn(bindResponse);
		expect(connection.isAuthenticated()).andStubReturn(true);
		expect(connection.search("OU=People,DC=example,DC=com", "(userPrincipalName=stuart@example.com)", SearchScope.SUBTREE)).andStubReturn(cursor);
		replay(connection);
		
		LdapConnectionPool pool = createMock(LdapConnectionPool.class);
		expect(pool.getConnection()).andStubReturn(connection);
		pool.releaseConnection(connection);
		expectLastCall();
		replay(pool);
		
		DomainLdapContext context = EasyMock.createMockBuilder(DomainLdapContext.class).addMockedMethod("getConnectionPool").createMock();
		expect(context.getConnectionPool()).andStubReturn(pool);
		replay(context);
		
		context.setSearchTemplate("OU=People,DC=example,DC=com");
		context.setFilterTemplate("(userPrincipalName={0})");
		context.setDomain("example.com");
		
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername("stuart");
		token.setPassword("password".toCharArray());
		
		AuthenticationInfo info = context.query(token, realm);
		Assert.assertNotNull(info);
		Assert.assertEquals(2, info.getPrincipals().asList().size());
		Assert.assertEquals("stuart@example.com", info.getPrincipals().getPrimaryPrincipal());
	}
	
	@Test 
	public void testQueryFailure() throws Exception {

		Realm realm = createMock(Realm.class);
		expect(realm.getName()).andStubReturn("mockrealm");
		replay(realm);

		LdapResult bindResult = createMock(LdapResult.class);
		expect(bindResult.getResultCode()).andStubReturn(ResultCodeEnum.UNAVAILABLE);
		expect(bindResult.getDiagnosticMessage()).andReturn("It went wrong");
		replay(bindResult);

		BindResponse bindResponse = createMock(BindResponse.class);
		expect(bindResponse.getLdapResult()).andStubReturn(bindResult);
		replay(bindResponse);

		LdapConnection connection = createMock(LdapConnection.class);
		expect(connection.bind(anyObject(BindRequest.class))).andStubReturn(bindResponse);
		replay(connection);
		
		LdapConnectionPool pool = createMock(LdapConnectionPool.class);
		expect(pool.getConnection()).andStubReturn(connection);
		pool.releaseConnection(connection);
		expectLastCall();
		replay(pool);
		
		DomainLdapContext context = EasyMock.createMockBuilder(DomainLdapContext.class).addMockedMethod("getConnectionPool").createMock();
		expect(context.getConnectionPool()).andStubReturn(pool);
		replay(context);
		
		context.setSearchTemplate("OU=People,DC=example,DC=com");
		context.setFilterTemplate("(userPrincipalName={0})");
		context.setDomain("example.com");
		
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername("stuart");
		token.setPassword("password".toCharArray());
		
		thrown.expect(AuthenticationException.class);
		thrown.expectMessage("It went wrong");

		context.query(token, realm);
	}
}
