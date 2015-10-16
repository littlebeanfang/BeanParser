package POSTaggerUtil;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;


public class MaltConllParse {
	
	public String Parse(String conllsent, MaltParserService service) throws MaltChainedException{
		String conllparseout="";
		String[] tokens=conllsent.split("\n");
		String[] parseout = service.parseTokens(tokens);
		// Outputs the dependency graph created by MaltParser.
		for(String ele:parseout){
			ele=ele+"\t_\t_\n";
			conllparseout=conllparseout+ele;
			//System.out.print(ele);
		}
		//System.out.println(conllparseout);
		return conllparseout;
	}
	public static void main(String args[]) throws MaltChainedException{
		MaltParserService service =  new MaltParserService();
		// Inititalize the parser model 'model0' and sets the working directory to '.' and sets the logging file to 'parser.log'
		service.initializeParserModel("-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log");
		MaltConllParse test=new MaltConllParse();
		String conllsent="1\tThis\t_\tDT\tDT\t_\n"
						+ "2\tis\t_\tVBZ\tVBZ\t_\n"
						+ "3\ta\t_\tDT\tDT\t_\t_\n"
						+ "4\tsample\t_\tNN\tNN\t_\n"
						+ "5\ttext\t_\tNN\tNN\t_\n";
		String conllparseout=test.Parse(conllsent, service);
		service.terminateParserModel();
	}
}
