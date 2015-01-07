package Test;


import java.io.IOException;

import DataStructure.Parameters;
import DataStructure.ParserOptions;
import Parser.DependencyPipe;
import Parser.MyPipe;
import Parser.Parser;

/**
 * Since we get the model of every iteration, and the average operation is very important for
 * improve the performance of param, let's have a test
 * 1. averge the latter 5 iteration
 * @author Wenjing
 *
 */
public class ParamAverageTest {
	private double[] total;
	/**
	 * args is the list of average model name.
	 * @param args
	 * @param modelfile
	 * @throws Exception 
	 */
	public void AvergeModelParam(String[] args, String newmodelfile, int numofinstance) throws Exception{
		//at least one model file name
		ParserOptions options = new ParserOptions(args);
		MyPipe dp=new MyPipe(options);
		Parser test=new Parser(dp,options);
		test.loadModel(args[0]);
		total=new double[test.GetParam().length];
		total=plus(total,test.GetParam());
		for(int i=1;i<args.length;i++){
			test=new Parser(dp,options);
			test.loadModel(args[0]);
			total=plus(total,test.GetParam());
		}
		int avVal=numofinstance*args.length;
		for(int j = 0; j < total.length; j++)
		    total[j] *= 1.0/((double)avVal);
		test.SetParam(total);
		test.saveModel(newmodelfile);
	}
	public double[] plus(double[] a, double[] b) {  
	    int hang = a.length;  
	    double[] result = new double[hang];  
	    for (int i = 0; i < hang; i++) {  
	        result[i] = a[i] + b[i];  
	    }  
	    return result;  
	} 
	public static void main(String args[]) throws Exception{
		//this model should be the same as bean_train_wsj2-21_godorder_beam1.model
		String models[]={"bean_train_wsj2-21_godorder_beam1_iter0.model",
		"bean_train_wsj2-21_godorder_beam1_iter1.model",
		"bean_train_wsj2-21_godorder_beam1_iter2.model",
		"bean_train_wsj2-21_godorder_beam1_iter3.model",
		"bean_train_wsj2-21_godorder_beam1_iter4.model",
		"bean_train_wsj2-21_godorder_beam1_iter5.model",
		"bean_train_wsj2-21_godorder_beam1_iter6.model",
		"bean_train_wsj2-21_godorder_beam1_iter7.model",
		"bean_train_wsj2-21_godorder_beam1_iter8.model",
		"bean_train_wsj2-21_godorder_beam1_iter9.model"
		};
		ParamAverageTest PAT=new ParamAverageTest();
		PAT.AvergeModelParam(models, "bean_train_wsj2-21_godorder_beam1_iteraveragetest_iter0-9.model", 39832);
	}
}
