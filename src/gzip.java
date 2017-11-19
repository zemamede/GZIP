/* Author: Rui Pedro Paiva
Teoria da Informa��o, LEI, 2006/2007*/

import java.io.*;
import java.util.Arrays;

//class principal para leitura de um ficheiro gzip
//M�todos:
//gzip(String fileName) throws IOException --> construtor
//int getHeader() throws IOException --> l� o cabe�alho do ficheiro para um objecto da class gzipHeader
//void closeFiles() throws IOException --> fecha os ficheiros
//String bits2String(byte b) --> converte um byte para uma string com a sua representa��o bin�ria
public class gzip
{
	static gzipHeader gzh;
	static String gzFile;
	static long fileSize;
	static long origFileSize;
	static int numBlocks = 0;
	static RandomAccessFile is;
	static int rb = 0, availBits = 0;


	public void calcHuffmanInsertTree (HuffmanTree tree, int[] arrayOriginal, int[] nextCode, int size){
		int[] codeF = new int[size];
		String string;
		for (int i=0;i<size;i++){

			if(arrayOriginal[i]!=0){
				codeF[i] = nextCode[arrayOriginal[i]-1];
				string = bits2String((byte)codeF[i]);
				System.out.printf(""+string);
				tree.addNode(string,i,false);
				nextCode[arrayOriginal[i]-1]++;
				System.out.println("Bits a inserir:"+codeF[i]+" Nr bits do codigo"+arrayOriginal[i]+" == "+string);
			}
		}
	}

	public int[] eachBitStartCode(int[] b1Count){
		int []nextCode = new int[b1Count.length+10];
		int code = 0;
		b1Count[0] = 0;
		for(int i=1;i<b1Count.length;i++){
			code = (code + b1Count[i-1])<<1;
			nextCode[i] = code;
		}
		System.out.println(Arrays.toString(nextCode));
		return  nextCode;
	}

	public int[] countNumberCodes(int[]arrayNoZeros){
		int [] extremos;
		int max;
		Arrays.sort(arrayNoZeros);
		extremos = minMaxLength(arrayNoZeros);
		max = extremos[1];
		int [] b1Count = new int[max];
		int counter = 0;
		for(int i=0;i<max;i++){

			for(int j=0;j<arrayNoZeros.length;j++){
				if(arrayNoZeros[j]==i+1){
					counter ++;
				}else{
					continue;
				}
			}
			b1Count[i] = counter;
			counter = 0;
		}
		System.out.println(Arrays.toString(b1Count));
		return b1Count;
	}

	public int[] minMaxLength(int[]array){
		int[] extremos = new int[2];
		//min
		extremos[0] = array[0];
		//max
		extremos[1] = array[array.length-1];
		return extremos;
	}

	public int[] removeZerosFromArray(int []array){
		int numZeros = countZerosFromArray(array);
		int[] result = new int[array.length-numZeros];
		int j=0;
		for(int i=0;i<array.length;i++){
			if(array[i]!=0){
				result[j] = array[i];
				j++;
			}
		}
		return  result;
	}

	public int countZerosFromArray(int[] array){
		int count = 0;
		for(int i=0;i<array.length;i++){
			if(array[i]==0){
				count++;
			}
		}
		return count;
	}

	public int[] orderCodeLength(int[] array, int HCLEN){
        int[] orderedArray = {16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15};
        int[] result = new int[19];
        for(int i=0;i<result.length;i++){
            if(i<HCLEN+4) {
                result[orderedArray[i]] = array[i];
            }
            else{
                result[orderedArray[i]]=0;
            }
        }
        array = result;
        return array;
    }

	public int readBits(int neededBits) throws IOException {
		int result;
		int m = (int) Math.pow(2,neededBits)-1;
        while(availBits<neededBits){
			this.rb = is.readUnsignedByte() << availBits | rb;
			availBits += 8;
		}
		result = rb&m;
    	rb = rb >> neededBits;
    	availBits -= neededBits;

		return result;
	}

	//fun��o principal, a qual gere todo o processo de descompacta��o
	public static void main (String args[])
	{

		//--- obter ficheiro a descompactar
		String fileName = "FAQ.txt.gz";
		/*if (args.length != 1)
		{
			System.out.println("Linha de comando inv�lida!!!");
			return;
		}
		String fileName = args[0];*/			
				
		//--- processar ficheiro
		try
		{
			gzip gz = new gzip(fileName);
			//System.out.println(fileSize);
			
			//ler tamanho do ficheiro original e definir Vector com s�mbolos
			origFileSize = getOrigFileSize();
			System.out.println(origFileSize);
			
			//--- ler cabe�alho
			int erro = getHeader();
			if (erro != 0)
			{
				System.out.println ("Formato inv�lido!!!");
				return;
			}
			//else				
			//	System.out.println(gzh.fName);
			
			
			//--- Para todos os blocos encontrados
			int BFINAL,HLIT,HDIST,HCLEN;
			
			do
			{				
				//--- ler o block header: primeiro byte depois do cabe�alho do ficheiro
/*
				needBits = 3;
				if (availBits < needBits)
				{
					rb = is.readUnsignedByte() << availBits | rb;
					availBits += 8;
				}*/

				//obter BFINAL
				//ver se � o �ltimo bloco
				BFINAL = gz.readBits(1); //primeiro bit � o menos significativo
				System.out.println("BFINAL = " + BFINAL);
								
				//analisar block header e ver se � huffman din�mico					
				if (!isDynamicHuffman(gz.readBits(2))) //ignorar bloco se n�o for Huffman din�mico
					continue;

                HLIT= gz.readBits(5);
                System.out.println("HLIT = " + HLIT);
                HDIST = gz.readBits(5);
                System.out.println("HDIST = " + HDIST);
                HCLEN = gz.readBits(4);
				System.out.println("HCLEN = " + HCLEN);
                int[] codeLength = new int[19];
                int[] codeLengthNoZeros;
                int[] b1Count;
                int[] nextCode;
                for(int i=0;i<HCLEN+4;i++){
                    codeLength[i]= gz.readBits(3);
                }
				HuffmanTree codeLengthtree = new HuffmanTree();

                codeLength = gz.orderCodeLength(codeLength,HCLEN);
				codeLengthNoZeros = gz.removeZerosFromArray(codeLength);
                b1Count = gz.countNumberCodes(codeLengthNoZeros);
                nextCode = gz.eachBitStartCode(b1Count);

				gz.calcHuffmanInsertTree(codeLengthtree,codeLength,nextCode,19);
				//actualizar n�mero de blocos analisados
				numBlocks++;				
			}while(BFINAL == 0);
						

			//termina��es			
			is.close();	
			System.out.println("End: " + numBlocks + " bloco(s) analisado(s).");
		}
		catch (IOException erro)
		{
			System.out.println("Erro ao usar o ficheiro!!!");
			System.out.println(erro);
		}
	}

	
	//Construtor: recebe nome do ficheiro a descompactar e cria File Streams
	gzip(String fileName) throws IOException
	{
		gzFile = fileName;
		is = new RandomAccessFile(fileName, "r");
		fileSize = is.length();
	}
	
	
	//Obt�m tamanho do ficheiro original
	public static long getOrigFileSize() throws IOException
	{
		//salvaguarda posi��o actual do ficheiro
		long fp = is.getFilePointer();
		
		//�ltimos 4 bytes = ISIZE;
		is.seek(fileSize-4);
		
		//determina ISIZE (s� correcto se cabe em 32 bits)
		long sz = 0;
		sz = is.readUnsignedByte();
		for (int i = 0; i <= 2; i++)
			sz = (is.readUnsignedByte() << 8*(i+1)) + sz;			
		
		//restaura file pointer
		is.seek(fp);
		
		return sz;		
	}
		

	//L� o cabe�alho do ficheiro gzip: devolve erro se o formato for inv�lido
	public static int getHeader() throws IOException  //obt�m cabe�alho
	{
		gzh = new gzipHeader();
		
		int erro = gzh.read(is);
		if (erro != 0) return erro; //formato inv�lido		
		
		return 0;
	}
		
	
	//Analisa block header e v� se � huffman din�mico
	public static boolean isDynamicHuffman(int BTYPE)
	{
						
		if (BTYPE == 0) //--> sem compress�o
		{
			System.out.println("Ignorando bloco: sem compacta��o!!!");
			return false;
		}
		else if (BTYPE == 1)
		{
			System.out.println("Ignorando bloco: compactado com Huffman fixo!!!");
			return false;
		}
		else if (BTYPE == 3)
		{
			System.out.println("Ignorando bloco: BTYPE = reservado!!!");
			return false;
		}
		else
			return true;
		
	}
	
	
	//Converte um byte para uma string com a sua representa��o bin�ria
	public static String bits2String(byte b)
	{
		String strBits = "";
		byte mask = 0x01;  //get LSbit
		
		for (byte bit, i = 1; i <= 8; i++)
		{
			bit = (byte)(b & mask);
			strBits = bit + strBits; //add bit to the left, since LSb first
			b >>= 1;
		}
		return strBits;		
	}

	public  static String bits2StringWithSize(byte b, int size){
		String strBits = "";
		byte mask = 0x01;  //get LSbit

		for (byte bit, i = 1; i <= 8; i++)
		{
			bit = (byte)(b & mask);
			strBits = bit + strBits; //add bit to the left, since LSb first
			b >>= 1;
		}
		return strBits;
	}
}