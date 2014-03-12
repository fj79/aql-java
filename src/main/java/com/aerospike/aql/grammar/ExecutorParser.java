package com.aerospike.aql.grammar;

import gnu.crypto.util.Base64;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Info;
import com.aerospike.client.Key;
import com.aerospike.client.Language;
import com.aerospike.client.Record;
import com.aerospike.client.ScanCallback;
import com.aerospike.client.Value;
import com.aerospike.client.cluster.Node;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.ScanPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.IndexType;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.ResultSet;
import com.aerospike.client.query.Statement;
import com.aerospike.client.task.RegisterTask;

/**
 * This class is a tree parser that can execute operations on an aerospike cluster
 * @author peter
 *
 */
public class ExecutorParser extends TreeParser{

	enum ViewType {
		JSON,
		TABLE
	}
	protected AerospikeClient client;
	protected IErrorReporter errorReporter;
	protected IResultReporter resultReporter;
	protected Policy policy;
	protected WritePolicy writePolicy;
	public ViewType viewType;

	public ExecutorParser(TreeNodeStream input, RecognizerSharedState state) {
		super(input, state);
	}

	public ExecutorParser(TreeNodeStream input) {
		super(input);
		this.resultReporter = new SystemOutReporter();
	}
	public ExecutorParser(TreeNodeStream input, AerospikeClient client) {
		this(input);
		this.client = client;
		this.policy = new Policy();
		this.policy.timeout = 0;
		this.writePolicy = new WritePolicy();
	}

	public ExecutorParser(TreeNodeStream input, RecognizerSharedState state, AerospikeClient client) {
		this(input, state);
		this.client = client;
		this.policy = new Policy();
		this.policy.timeout = 0;
		this.writePolicy = new WritePolicy();
	}

	protected Node randomNode(){
		if (isConnected())
			return client.getNodes()[0];
		else
			return null;
	}

	protected boolean isConnected(){
		return (this.client != null && this.client.isConnected());
	}

	protected String removeQuotes(String source){
		String target = source.substring(1, source.length()-1);
		return target;
	}

	protected HashMap<String, String> info(String... cmds) throws AerospikeException{
		Node node = randomNode();
		if (node != null)
			return Info.request(node.getHost().name, node.getHost().port, cmds);
		return null;
	}

	protected String info(String cmd) throws AerospikeException{
		Node node = randomNode();
		if (node != null) {
			return Info.request(node.getHost().name, node.getHost().port, cmd);
		}
		return null;
	}
	protected void printInfo(String title, String infoString){
		if (infoString == null || infoString.isEmpty())
			return;
		String[] outerParts = infoString.split(";");
		System.out.println(title);
		for (String s : outerParts){

			String[] innerParts = s.split(":");
			for (String parts : innerParts){
				System.out.println("\t" + parts);
			}
			System.out.println();
		}

	}

	public IErrorReporter getErrorReporter() {
		return errorReporter;
	}


	public void setErrorReporter(IErrorReporter errorReporter) {
		this.errorReporter = errorReporter;
	}

	public IResultReporter getResultReporter() {
		return resultReporter;
	}

	public void setResultReporter(IResultReporter resultReporter) {
		this.resultReporter = resultReporter;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (isConnected())
			this.client.close();
	}

	protected void reportStatement(String statementText){
		this.resultReporter.report(statementText);
	}

	public String getUdfSource(String info) throws UnsupportedEncodingException{
		//gen=qgmyp0d8hQNvJdnR42X3BXgUGPE=;type=LUA;content=bG9jYWwgZnVuY3Rpb24gcHV0QmluKHIsbmFtZSx2YWx1ZSkKICAgIGlmIG5vdCBhZXJvc3Bpa2U6ZXhpc3RzKHIpIHRoZW4gYWVyb3NwaWtlOmNyZWF0ZShyKSBlbmQKICAgIHJbbmFtZV0gPSB2YWx1ZQogICAgYWVyb3NwaWtlOnVwZGF0ZShyKQplbmQKCi0tIFNldCBhIHBhcnRpY3VsYXIgYmluCmZ1bmN0aW9uIHdyaXRlQmluKHIsbmFtZSx2YWx1ZSkKICAgIHB1dEJpbihyLG5hbWUsdmFsdWUpCmVuZAoKLS0gR2V0IGEgcGFydGljdWxhciBiaW4KZnVuY3Rpb24gcmVhZEJpbihyLG5hbWUpCiAgICByZXR1cm4gcltuYW1lXQplbmQKCi0tIFJldHVybiBnZW5lcmF0aW9uIGNvdW50IG9mIHJlY29yZApmdW5jdGlvbiBnZXRHZW5lcmF0aW9uKHIpCiAgICByZXR1cm4gcmVjb3JkLmdlbihyKQplbmQKCi0tIFVwZGF0ZSByZWNvcmQgb25seSBpZiBnZW4gaGFzbid0IGNoYW5nZWQKZnVuY3Rpb24gd3JpdGVJZkdlbmVyYXRpb25Ob3RDaGFuZ2VkKHIsbmFtZSx2YWx1ZSxnZW4pCiAgICBpZiByZWNvcmQuZ2VuKHIpID09IGdlbiB0aGVuCiAgICAgICAgcltuYW1lXSA9IHZhbHVlCiAgICAgICAgYWVyb3NwaWtlOnVwZGF0ZShyKQogICAgZW5kCmVuZAoKLS0gU2V0IGEgcGFydGljdWxhciBiaW4gb25seSBpZiByZWNvcmQgZG9lcyBub3QgYWxyZWFkeSBleGlzdC4KZnVuY3Rpb24gd3JpdGVVbmlxdWUocixuYW1lLHZhbHVlKQogICAgaWYgbm90IGFlcm9zcGlrZTpleGlzdHMocikgdGhlbiAKICAgICAgICBhZXJvc3Bpa2U6Y3JlYXRlKHIpIAogICAgICAgIHJbbmFtZV0gPSB2YWx1ZQogICAgICAgIGFlcm9zcGlrZTp1cGRhdGUocikKICAgIGVuZAplbmQKCi0tIFZhbGlkYXRlIHZhbHVlIGJlZm9yZSB3cml0aW5nLgpmdW5jdGlvbiB3cml0ZVdpdGhWYWxpZGF0aW9uKHIsbmFtZSx2YWx1ZSkKICAgIGlmICh2YWx1ZSA+PSAxIGFuZCB2YWx1ZSA8PSAxMCkgdGhlbgogICAgICAgIHB1dEJpbihyLG5hbWUsdmFsdWUpCiAgICBlbHNlCiAgICAgICAgZXJyb3IoIjEwMDA6SW52YWxpZCB2YWx1ZSIpIAogICAgZW5kCmVuZAoKLS0gUmVjb3JkIGNvbnRhaW5zIHR3byBpbnRlZ2VyIGJpbnMsIG5hbWUxIGFuZCBuYW1lMi4KLS0gRm9yIG5hbWUxIGV2ZW4gaW50ZWdlcnMsIGFkZCB2YWx1ZSB0byBleGlzdGluZyBuYW1lMSBiaW4uCi0tIEZvciBuYW1lMSBpbnRlZ2VycyB3aXRoIGEgbXVsdGlwbGUgb2YgNSwgZGVsZXRlIG5hbWUyIGJpbi4KLS0gRm9yIG5hbWUxIGludGVnZXJzIHdpdGggYSBtdWx0aXBsZSBvZiA5LCBkZWxldGUgcmVjb3JkLiAKZnVuY3Rpb24gcHJvY2Vzc1JlY29yZChyLG5hbWUxLG5hbWUyLGFkZFZhbHVlKQogICAgbG9jYWwgdiA9IHJbbmFtZTFdCgogICAgaWYgKHYgJSA5ID09IDApIHRoZW4KICAgICAgICBhZXJvc3Bpa2U6cmVtb3ZlKHIpCiAgICAgICAgcmV0dXJuCiAgICBlbmQKCiAgICBpZiAodiAlIDUgPT0gMCkgdGhlbgogICAgICAgIHJbbmFtZTJdID0gbmlsCiAgICAgICAgYWVyb3NwaWtlOnVwZGF0ZShyKQogICAgICAgIHJldHVybgogICAgZW5kCgogICAgaWYgKHYgJSAyID09IDApIHRoZW4KICAgICAgICByW25hbWUxXSA9IHYgKyBhZGRWYWx1ZQogICAgICAgIGFlcm9zcGlrZTp1cGRhdGUocikKICAgIGVuZAplbmQKCi0tIFNldCBleHBpcmF0aW9uIG9mIHJlY29yZAotLSBmdW5jdGlvbiBleHBpcmUocix0dGwpCi0tICAgIGlmIHJlY29yZC50dGwocikgPT0gZ2VuIHRoZW4KLS0gICAgICAgIHJbbmFtZV0gPSB2YWx1ZQotLSAgICAgICAgYWVyb3NwaWtlOnVwZGF0ZShyKQotLSAgICBlbmQKLS0gZW5kCg==;
		Map<String, String> map = new HashMap<String, String>();
		String[] udfparts = info.split(";");
		for (String kv : udfparts){
			String[] kvParts = kv.split("=");
			map.put(kvParts[0], kvParts[1]);
		}
		String code = map.get("content");
		code = code.substring(0, code.length()-2);
		String source = new String(Base64.decode(code));
		return source;

	}

	public void describeIndex(String namespace, String indexname) throws AerospikeException{
		printInfo("Describe Index", info("sindex-describe:ns=" + namespace + ";indexname=" + indexname +";"));
	}

	public void describeModule(String moduleName) throws AerospikeException{
		this.resultReporter.report("Module: " + moduleName);
		String udfString = info("udf-get:filename=" + moduleName );
		if (udfString.startsWith("error")){
			this.resultReporter.report(udfString);
		} else {
			try {
				this.resultReporter.report(getUdfSource(udfString));
			} catch (UnsupportedEncodingException e) {
			}
		}

	}

	public void executeAggregation(String namespace, String set, String packageName, String functionName, List<Value> values, Filter filter) throws AerospikeException{
		Statement stmt = new Statement();
		stmt.setNamespace(namespace);
		stmt.setSetName(set);
		stmt.setFilters(filter);
		ResultSet resultSet = client.queryAggregate(null, stmt, 
				packageName, 
				functionName, 
				values.toArray(new Value[values.size()]));
		try {
			int count = 0;

			while (resultSet.next()) {
				Object object = resultSet.getObject();
				this.resultReporter.report("Result: " + object);
				count++;
			}

			if (count == 0) {
				this.resultReporter.report("No results returned.");     
			}
		}
		finally {
			resultSet.close();
		}
	}

	public void registerPackage(String packagePath) throws AerospikeException{
		File udfFile = null;
		if (packagePath.startsWith("'"))
			udfFile = new File(removeQuotes(packagePath));
		else
			udfFile = new File(packagePath);
		if (!udfFile.exists()){
			this.resultReporter.report("UDF file does not exist: " + packagePath); 
			return;
		}
		RegisterTask task = this.client.register(null, 
				udfFile.getPath(), 
				udfFile.getName(),
				Language.LUA); 
		task.waitTillComplete();

	}

	public void removePackage(String packageName) throws AerospikeException{
		Node node = this.client.getNodes()[0];
		String msg = Info.request(node, "udf-remove:filename=" + packageName);
		if (msg.contains("error")){
			this.resultReporter.report("Could not delete module: " +  packageName);
		} else {
			this.resultReporter.report("Delete module: " +  packageName);
		}

	}

	protected Value getValue(String valueString){
		Value value = null;
		try {
			value = Value.get(Integer.decode(valueString));
		} catch (NumberFormatException e){
			value = Value.get(valueString);
		}
		return value;
	}

	protected Key getKey(String namespace, String set, String keyvalue) throws AerospikeException {
		Key key = new Key(namespace, set, getValue(keyvalue));
		return key;
	}

	public void deleteRecord(String namespace, String set, String keyvalue) throws AerospikeException{
		this.client.delete(this.writePolicy, getKey(namespace, set, keyvalue));
	}

	public void insertRecord(String namespace, String set, String keyvalue, List<String> binNames, List<Value> binValues) throws AerospikeException{
		Bin[] bins = new Bin[binNames.size()];
		if (binNames.size() > 0){
			int count = 0;
			for (String name : binNames){
				bins[count] = new Bin(name, binValues.get(count));
				count++;
			}
		}
		Key key = getKey(namespace, set, keyvalue);
		StringBuilder sb = new StringBuilder();
		long start_time = System.nanoTime();
		if (bins.length > 0)
			client.put(this.writePolicy, key, bins);
		else 
			client.put(this.writePolicy, key);
		long end_time = System.nanoTime();
		double difference = (end_time-start_time)/1000000.0;
		sb.append("Success in ");
		sb.append(difference);
		sb.append("ms");
		this.resultReporter.report(sb.toString());

	}

	public void createIndex(String namespace, String set, String indexName, String binName, String type) throws AerospikeException{
		if (isConnected()) {
			long start_time = System.nanoTime();
			this.client.createIndex(policy, namespace, set, indexName, binName, (type.equalsIgnoreCase("string")) ? IndexType.STRING : IndexType.NUMERIC);
			long end_time = System.nanoTime();
			double difference = (end_time-start_time)/1000000.0;
			this.resultReporter.report("Index " + namespace + "." + set + "." + indexName + " - created in " + difference + "ms");
		}	

	}

	public void selectRecord(String namespace, String set, List<String> binNames, String keyString, Filter filter) throws AerospikeException{
		StringBuilder sb = new StringBuilder();
		long start_time = System.nanoTime();
		long end_time = 0;
		if (keyString != null && !keyString.isEmpty()){
			// singleton
			Record record = null;
			if (binNames != null && binNames.size() > 0){
				record = client.get(this.policy, getKey(namespace, set, keyString), binNames.toArray(new String[binNames.size()]));
			} else {
				record = client.get(this.policy, getKey(namespace, set, keyString));
			}
			end_time = System.nanoTime();
			this.resultReporter.report("Record: " + record);

		} else if (filter != null){
			// query
			Statement stmt = new Statement();
			if (binNames != null && binNames.size() > 0){
				stmt.setBinNames(binNames.toArray(new String[binNames.size()]));
			}
			stmt.setFilters(filter);
			// Execute the query
			RecordSet recordSet = client.query(null, stmt);

			// Process the record set
			try {
				while (recordSet != null && recordSet.next()) {
					Key key = recordSet.getKey();
					Record record = recordSet.getRecord();

					resultReporter.report("Key: "+ key + " Record: " + record);

				}
			}
			finally {
				recordSet.close();
			}

			end_time = System.nanoTime();
		} else {
			// scan
			ScanPolicy scanPolicy = new ScanPolicy();
			this.client.scanAll(scanPolicy, namespace, set, new ScanCallback() {

				@Override
				public void scanCallback(Key key, Record record) throws AerospikeException {
					resultReporter.report("Record: " + record);

				}
			});
			end_time = System.nanoTime();
		}

		double difference = (end_time-start_time)/1000000.0;
		sb.append("Success in ");
		sb.append(difference);
		sb.append("ms");
		this.resultReporter.report(sb.toString());
	}
}
