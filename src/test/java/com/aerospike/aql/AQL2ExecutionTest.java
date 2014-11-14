package com.aerospike.aql;
import java.io.File;

import org.junit.Test;

import com.aerospike.aql.v2.AQL2;
import com.aerospike.aql.v2.AQLGenerator.Language;
import com.aerospike.client.AerospikeClient;

public class AQL2ExecutionTest {
	private AerospikeClient client = new AerospikeClient("127.0.0.1", 3000);
	
	@Test
	public void testJavaExecuteShowBins() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("show bins");
	}
	@Test
	public void testJavaExecuteShowModules() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("show modules");
	}
	@Test
	public void testJavaExecuteShowNamespace() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("show namespaces");
	}
	@Test
	public void testJavaExecuteShowSets() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("show sets");
	}
	@Test
	public void testJavaExecuteShowScans() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("show scans");
	}
	@Test
	public void testJavaExecuteShowQueries() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("show Queries");
	}
	@Test
	public void testJavaExecuteShowIndexes1() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("show InDexes test.demo");
	}
	@Test
	public void testJavaExecuteShowIndexes2() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("show InDexes");
	}
	@Test
	public void testJavaExecuteDescIndexe() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("desc InDex test index_bn2");
	}
	@Test
	public void testJavaExecuteStatQuery() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("stat query");
	}
	@Test
	public void testJavaExecuteStatSystem() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("stat system");
	}
	@Test
	public void testJavaExecutePrint() throws Exception {
		AQL2 aql2 = new AQL2(client, 20);
		aql2.execute("print 'hello there'");
	}

	@Test
	public void testExecuteFileEverything() throws Exception {
		AQL2 aql2 = new AQL2();
		File inputFile = new File("src/test/resources/Everything.aql");
		aql2.execute(inputFile);
		
	}

}
