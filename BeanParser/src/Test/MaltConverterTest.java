package Test;

import java.io.File;
import java.io.IOException;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import DOSH.Converter;
import DOSH.DOSHTrain;
import Malt.MaltConverter;
import Malt.MaltTrain;

public class MaltConverterTest {
	public static void main(String args[]) throws IOException, InvalidInputDataException, ClassNotFoundException{
		String conllfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.conll";
		String maltfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.malt";
		String maltgold=System.getenv("CODEDATA")+File.separator+"wsj_2-21_malt_processindex.txt";
		String senconll=System.getenv("CODEDATA")+File.separator+"wsj_2-21.conll";
		String senlib=System.getenv("CODEDATA")+File.separator+"Order//wsj_2-21.maltlibins";
		String senorder=System.getenv("CODEDATA")+File.separator+"Order//wsj_2-21.order";
		String senalphabet=System.getenv("CODEDATA")+File.separator+"Order//wsj_2-21.maltalphabet";
		String senlibmodel=System.getenv("CODEDATA")+File.separator+"Order//wsj_2-21.libmodel";
		MaltConverter test=new MaltConverter();
		test.Convert(conllfile,maltfile);
//		Converter ordercompare=new Converter();
//		ordercompare.OrderCompare("wsj_2-21.malt","wsj_2-21.maltliborder" );
		
		
//		MaltTrain test=new MaltTrain();
//		test.GenerateLiblinearAndOrderFile(senconll, senlib, senorder, senalphabet);
//		DOSHTrain train=new DOSHTrain();
//		train.TrainLiblinear(senlib, senlibmodel);
//		train.TestModelAccuracyOnInstanceFile(senlibmodel, senlib);
	}
}
