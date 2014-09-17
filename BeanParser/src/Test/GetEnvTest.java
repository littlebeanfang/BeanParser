package Test;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

public class GetEnvTest {
	public static void main(String args[]){
		Iterator iterator=System.getenv().entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, String> env=(Entry<String, String>)iterator.next();
			System.out.println("Key:"+env.getKey()+", Value:"+env.getValue());
		}
		System.out.println(System.getenv("CODEDATA")+File.separator);
	}
}
