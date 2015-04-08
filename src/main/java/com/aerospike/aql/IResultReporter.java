package com.aerospike.aql;

import java.util.Map;

import javax.xml.stream.events.Namespace;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Log.Level;
import com.aerospike.client.Record;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.ResultSet;
/**
 * This interface is used to report results to
 * the calling application
 * @author peter
 *
 */
public interface IResultReporter {
	enum ViewFormat {
		JSON,
		TABLE,
		TEXT
	}
	public void report(String message);
	public void report(AerospikeException e);
	public void report(Level level, String message);
	public void report(Record record);
	public void report(Key key, Record record);
	public void report(RecordSet recordSet);
	public void report(ResultSet resultSet);
	public void report(String message, boolean clear);
	public void report(Level level, String message, boolean clear);
	public void report(Record record, boolean clear);
	public void report(Key key, Record record, boolean clear);
	public void report(RecordSet recordSet, boolean clear);
	public void report(ResultSet resultSet, boolean clear);
	public void reportInfo(Map<String, String>[] ifoResults);
	public void reportInfo(String inforMessage, String...seperators);
	public void reportInfo(String inforMessage, boolean clear, String...seperators);
	public void reportInfo(String[] inforMessages, String...seperators);
	public void reportInfo(String[] inforMessages, boolean clear, String...seperators);
	public void cancel();
	public boolean isCancelled();
	public void setViewFormat(ViewFormat format);
}