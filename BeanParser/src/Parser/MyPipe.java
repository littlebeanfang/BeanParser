package Parser;

import java.io.IOException;

import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;

public class MyPipe extends DependencyPipe{
	public MyPipe(ParserOptions options) throws IOException {
		super(options);
		// TODO Auto-generated constructor stub
	}

	public void AddNewFeature(DependencyInstance inst, int childindex, int parentindex, ParseAgenda pa, FeatureVector fv){
		
	}
}
