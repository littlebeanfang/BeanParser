package IO;

import gnu.trove.TIntIntHashMap;

import java.io.IOException;

import DataStructure.DependencyInstance;

public class WriteInstanceWithOrder extends DependencyWriter{

	@Override
	public void write(DependencyInstance instance) throws IOException {
		// instance.order is order_child map
		TIntIntHashMap child_order=new TIntIntHashMap();
		for(int i=1;i<=instance.length();i++){
			child_order.put(instance.orders.get(i), i);
		}
		for (int i=1; i<instance.length(); i++) {
		    writer.write(Integer.toString(i));                writer.write('\t');
		    writer.write(instance.forms[i]);                    writer.write('\t');
		    writer.write(instance.forms[i]);                    writer.write('\t');
		    //writer.write(instance.cpostags[i]);                 writer.write('\t');
		    writer.write(instance.postags[i]);                  writer.write('\t');
		    writer.write(instance.postags[i]);                  writer.write('\t');
		    writer.write("-");                                  writer.write('\t');
		    writer.write(Integer.toString(instance.heads[i]));  writer.write('\t');
		    writer.write(instance.deprels[i]);                  writer.write('\t');
		    writer.write("-\t-\t"+child_order.get(i));
		    
		    writer.newLine();
		}
		writer.newLine();
	}

}
