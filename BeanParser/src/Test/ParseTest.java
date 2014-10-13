package Test;

import java.io.IOException;

import DataStructure.ParserOptions;
import Parser.DependencyPipe;
import Parser.MyPipe;
import Parser.Parser;

public class ParseTest {
	public static void main(String args[]) throws IOException{
		ParserOptions options = new ParserOptions(args);
		DependencyPipe dp=new MyPipe(options);
		Parser test=new Parser(dp,options);
		test.Parse(options.testfile);
	}
}
