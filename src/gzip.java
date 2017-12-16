


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;



public class gzip
{
	static gzipHeader gzh;
	static String gzFile;
	static long fileSize;
	static long origFileSize;
	static int numBlocks = 0;
	static RandomAccessFile is;
	static int rb = 0, availBits = 0;
	static ArrayList <String> outputF = new ArrayList<>();
	public void readDataBytes(HuffmanTree treeHLIT, HuffmanTree treeHDIST) throws IOException
	{
		int position;
		int backwardDistance;
		while(true){
			//decode literal length
			String bufferHLIT = "";
			while((position=treeHLIT.findNode(bufferHLIT,false))<0){
				bufferHLIT += readBits(1);
			}
			if(position<256){
				outputF.add(position+"");
			}
			else if(position == 256){
				break;
			}
			else{
				int length=0;
				if (position < 265) {
					length = position - 257 + 3;
				} else if (position == 285) {
					length = 258;
				} else{
					int counter = 0;
					int h = 1;
					int lengthE = 11;
					for(int j=265;j<285;j++){
						if(counter == 4){
							counter = 0;
							h++;
						}
						if(position==j){
							length = readBits(h)+lengthE;
							break;
						}else{
							lengthE +=(int)Math.pow(2,h);
						}
						counter++;
					}
				}
				//decode distance
				String bufferHDIST = "";
				while((position=treeHDIST.findNode(bufferHDIST,false))<0){
					bufferHDIST+= readBits(1);
				}
				if (position<4){
					backwardDistance = position +1;
					for(int i=0;i<length;i++) {
						outputF.add(outputF.get(outputF.size() - backwardDistance));
					}
				}else{
					int counter = 0;
					int h = 1;
					int dist = 5;
					for(int j=4;j<30;j++){
						if(counter == 2){
							counter = 0;
							h++;
						}
						if(position==j){
							backwardDistance = readBits(h) + dist;
							for(int i=0;i<length;i++) {
								outputF.add(outputF.get(outputF.size()-backwardDistance));
							}
							break;
						}else{
							dist +=(int)Math.pow(2,h);
						}
						counter++;
					}
				}
			}
		}
	}

	public int[] literalsDistanceArray(HuffmanTree tree,int size) throws IOException
	{
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
		return array;
	}

	public void huffmanFinal(HuffmanTree tree, int[] codeLength,int sizeVar, int sizeTotal)
	{
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

	public void calcHuffmanInsertTree (HuffmanTree tree, int[] arrayOriginal,int[] arrayNoZeros,int size)
	{
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
				tree.addNode(string,access[i],false);
				nextCode[arrayOriginal[i]-1]++;
			}
		}
	}

	public int[] removeZerosFromArray(int []array)
	{
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

	public int countZerosFromArray(int[] array)
	{
		int count = 0;
		for(int i=0;i<array.length;i++){
			if(array[i]==0){
				count++;
			}
		}
		return count;
	}

	public int[] orderCodeLength(int[] array, int HCLEN)
	{
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

	public int readBits(int neededBits) throws IOException
	{
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

	public static void main (String args[])
	{
		HuffmanTree codeLengthTree = new HuffmanTree();
		HuffmanTree literalTree = new HuffmanTree();
		HuffmanTree distanceTree = new HuffmanTree();
		String fileName = "FAQ.txt.gz";

		try
		{
			gzip gz = new gzip(fileName);
			origFileSize = getOrigFileSize();
			System.out.println("File size: "+origFileSize);

			int header = getHeader();
			if (header != 0)
			{
				System.out.println ("Invalid format !");
				return;
			}

			int BFINAL,HLIT,HDIST,HCLEN;
			
			do
			{
				BFINAL = gz.readBits(1);
				System.out.println("BFINAL = " + BFINAL);
				if (!isDynamicHuffman(gz.readBits(2)))
					continue;
                HLIT= gz.readBits(5);
                System.out.println("HLIT = " + HLIT);
                HDIST = gz.readBits(5);
                System.out.println("HDIST = " + HDIST);
                HCLEN = gz.readBits(4);
				System.out.println("HCLEN = " + HCLEN);

				/*Tree 1*/
				int[] codeLength = new int[19];
				for(int i=0;i<HCLEN+4;i++){
					codeLength[i]= gz.readBits(3);
				}
				gz.huffmanFinal(codeLengthTree,codeLength,HCLEN,19);
				/*Tree 2*/
				int[] literalLength = gz.literalsDistanceArray(codeLengthTree,HLIT+257);
				gz.huffmanFinal(literalTree,literalLength,HLIT,HLIT+257);
				/*Tree 3*/
				int[] distanceLength = gz.literalsDistanceArray(codeLengthTree,HDIST+1);
				gz.huffmanFinal(distanceTree,distanceLength,HDIST,HDIST+1);
				gz.readDataBytes(literalTree,distanceTree);

				byte[] output = new byte[outputF.size()];
				for(int i=0;i<outputF.size();i++){
					output[i] = (byte) Integer.parseInt(outputF.get(i));
				}
				try{
					FileOutputStream o = new FileOutputStream(gzh.fName);
					o.write(output);
					o.close();
				}catch (Exception e){
					System.out.println("Error trying to decompress!");
				}

				numBlocks++;

			}while(BFINAL == 0);



			System.out.println(gzh.fName+" decompressed!!");
			is.close();	
			System.out.println("End: " + numBlocks + " bloco(s) analisado(s).");
		}
		catch (IOException e)
		{
			System.out.println("Erro ao usar o ficheiro!!!");
			System.out.println(e);
		}
	}

	gzip(String fileName) throws IOException
	{
		gzFile = fileName;
		is = new RandomAccessFile(fileName, "r");
		fileSize = is.length();
	}

	public static long getOrigFileSize() throws IOException
	{
		long fp = is.getFilePointer();
		is.seek(fileSize-4);
		long sz = 0;
		sz = is.readUnsignedByte();
		for (int i = 0; i <= 2; i++)
			sz = (is.readUnsignedByte() << 8*(i+1)) + sz;
		is.seek(fp);
		return sz;		
	}

	public static int getHeader() throws IOException
	{
		gzh = new gzipHeader();
		
		int erro = gzh.read(is);
		if (erro != 0) return erro;
		
		return 0;
	}

	public static boolean isDynamicHuffman(int BTYPE)
	{
						
		if (BTYPE == 0)
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

	public  static String bits2StringWithSize(byte b, int size)
	{
		String strBits = "";
		byte mask = 0x01;
		for (byte bit, i = 1; i <= size; i++)
		{
			bit = (byte)(b & mask);
			strBits = bit + strBits;
			b >>= 1;
		}
		return strBits;
	}
}