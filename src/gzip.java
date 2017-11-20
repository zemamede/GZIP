/* Author: Rui Pedro Paiva
Teoria da Informa��o, LEI, 2006/2007*/

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

	public int[] literalsDistanceArray(HuffmanTree tree,int size) throws IOException {
		int[] array = new int[size];

		int position;
		int aux;
		for (int i = 0; i < array.length;) {
			String buff = "";

			while((position=tree.findNode(buff,false))<0){
				buff += readBits(1);
			}
			if(position<16){
				array[i] = position;
				i++;
			}
			if (position == 16) {
				aux = readBits(2) + 3;
				for (int j=0; j < aux; j++) {
					array[j+i] = array[i - 1];
				}
				i += aux;
			} else if (position == 17) {
				i += readBits(3) + 3;
			} else if (position == 18) {
				i += readBits(7) + 11;
			}
		}
		System.out.println(Arrays.toString(array));
		return array;
	}

	public void calcHuffmanInsertTree (HuffmanTree tree, int[] arrayOriginal,int[] arrayNoZeros,int size){
		int max;
		Arrays.sort(arrayNoZeros);
		max = arrayNoZeros[arrayNoZeros.length-1];
		int [] b1Count = new int[max];
		int counter = 0;
		for(int i=0;i<max;i++){
			for(int j=0;j<arrayNoZeros.length;j++){
				if(arrayNoZeros[j]==i+1){
					counter ++;
				}
			}
			b1Count[i] = counter;
			counter = 0;
		}
		b1Count[0] = 0;

		int []nextCode = new int[b1Count.length+10];
		int code = 0;
		for(int i=1;i<b1Count.length;i++){
			code = (code + b1Count[i-1])<<1;
			nextCode[i] = code;
		}

		int[] codeF = new int[size];
		int[] access = new int[size];
		String string;
		for(int i=0;i<size;i++){
			access[i] = i;
		}
		for (int i=0;i<size;i++){
			if(arrayOriginal[i]!=0){
				codeF[i] = nextCode[arrayOriginal[i]-1];
				string = bits2StringWithSize((byte)codeF[i],arrayOriginal[i]);
				tree.addNode(string,access[i],true);
				nextCode[arrayOriginal[i]-1]++;
				//System.out.println("Inserido o valor: "+string+" com "+codeF[i]+" bits\n");
			}
		}
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
			System.out.println("File size: "+origFileSize);
			
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

				HuffmanTree codeLengthTree = new HuffmanTree();
				HuffmanTree literalTree = new HuffmanTree();
				HuffmanTree distanceTree = new HuffmanTree();

				/*1*/
				int[] codeLength = new int[19];
				for(int i=0;i<HCLEN+4;i++){
					codeLength[i]= gz.readBits(3);
				}
				gz.huffmanFinal(codeLengthTree,codeLength,HCLEN,19);
				System.out.println("\n\n#DONE n1#\n\n");

				/*2*/
				int[] literalLength = gz.literalsDistanceArray(codeLengthTree,HLIT+257);
				gz.huffmanFinal(literalTree,literalLength,HLIT,HLIT+257);
				System.out.println("\n\n#DONE n2#\n\n");

				/*3*/
				int[] distanceLength = gz.literalsDistanceArray(codeLengthTree,HDIST+1);
				gz.huffmanFinal(distanceTree,distanceLength,HDIST,HDIST+1);
				System.out.println("\n\n#DONE n3#\n\n");


				//actualizar numero de blocos analisados
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

	public void huffmanFinal(HuffmanTree tree, int[] codeLength,int sizeVar, int sizeTotal){
		int[] arrayNoZeros;
		int[] res;
		if(codeLength.length==19) {
			res = orderCodeLength(codeLength, sizeVar);
		}else{
			res = codeLength;
		}
		arrayNoZeros = removeZerosFromArray(res);
		calcHuffmanInsertTree(tree,res,arrayNoZeros,sizeTotal);

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

		for (byte bit, i = 1; i <= size; i++)
		{
			bit = (byte)(b & mask);
			strBits = bit + strBits; //add bit to the left, since LSb first
			b >>= 1;
		}
		return strBits;
	}
}