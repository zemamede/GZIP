/* Author: Rui Pedro Paiva
Teoria da Informa��o, LEI, 2006/2007*/


//class para cria��o, gest�o e acesso de �rvores de Huffman
public class HuffmanTree
{
	HFNode root, curNode;  //ra�z da �rvore e n� actual na travessia
		

	//construtor	
	public HuffmanTree ()
	{
		root = new HFNode(-1, 0, null, null);
		curNode = root;
	}
	
	public HuffmanTree (HFNode rt, HFNode cr)		
	{
		root = rt;
		curNode = cr;		
	}
	

	//reposicionar n� corrente na ra�z
	public void resetCurNode ()
	{
		curNode = root;
	}
	
	
	//adiciona n� � �rvore 
	//recebe string com o c�digo (representado por String com 0s e 1s) e o �ndice no alfabeto
	//devolve -1 se o n� j� existe; -2 se o c�digo deixar de ser de prefixo; 0 se adicionou bem
	public int addNode(String s, int ind, boolean verbose)
	{
		HFNode tmp = root;
		int lv = 0, len = s.length(), index;
		boolean found = false;
		char direction;
		int pos = -3;
					
		while(lv < len && !found)
		{			
			if (tmp.index != -1)  //tentando criar filho de folha --> deixaria de ser c�digo de prefixo...
			{
				pos = -2;
				found = true;
			}
			else
			{			
				direction = s.charAt(lv);
				
				if (direction == '0')
				{
					if (lv != len-1 && tmp.left != null)  //continua descida
					{					
						tmp = tmp.left;
					}
					else if (tmp.left != null) // n� j� inserido
					{
						pos = -1;
						found = true;
					}
					/*else if (tmp.index != -1)  //tentando criar filho de folha --> deixaria de ser c�digo de prefixo...
					{
						pos = -2;
						found = true;
					}*/
					else //cria n� � esquerda
					{
						if (lv == len-1)  //folha						
							index = ind;
						else
							index = -1;

						HFNode hf = new HFNode(index, lv+1, null, null);
						tmp.left = hf;
						tmp = tmp.left;
					}
				}
				else if (direction == '1')
				{				
					if (lv != len -1 && tmp.right != null)
					{					
						tmp = tmp.right;
					}
					else if (tmp.right != null) // n� j� inserido
					{
						  pos = -1;
						  found = true;
					}
					/*else if (tmp.index != -1)
					{
						pos = -2;
						found = true;	
					}*/
					else //cria n� � direita
					{
						if (lv == len-1)  //folha
							index = ind;
						else
							index = -1;

						HFNode hf = new HFNode(index, lv+1, null, null);
						tmp.right = hf;
						tmp = tmp.right;
					}			
				}
			}				
			lv++;				
		}
		
				
		if (!found)
			pos = tmp.index;
			
		if (verbose)
		{
			if (pos == -1)
				System.out.println("Codigo '" + s + "' ja inserido!!!");
			else if (pos == -2)
				System.out.println("Codigo '" + s + ": tentando extender folha!!!");
			else
				System.out.println("Codigo '" + s + "' inserido com sucesso com o valor "+ind);
		}
		
		return pos;
	}				

	
	//procura c�digo na �rvore, a partir do n� actual (representado por String com 0s e 1s
	//devolve -1 se n�o encontrou; -2 se � prefixo de c�digo existente; �ndice no alfabeto se encontrou
	public int findNode(String s, HFNode cur, boolean verbose)
	{
		HFNode tmp = cur;
		int lv = 0, len = s.length();
		boolean found = true;
		int pos;
		
		while(lv < len && found)
		{			
			char direction = s.charAt(lv);
			
			if (direction == '0')
			{
				if (tmp.left != null)
					tmp = tmp.left;
				else
					found = false;
			}
			else if (direction == '1')
			{
				if (tmp.right != null)
					tmp = tmp.right;
				else
					found = false;
			}
			
			lv++;
		}
				
		if (!found)
			pos = -1;
		else if(tmp.index == -1)
			pos = -2;
		else
			pos = tmp.index;
			
		if (verbose)
		{
			if (pos == -1)
				System.out.println("C�digo '" + s + "' n�o encontrado!!!");
			else if (pos == -2)
				System.out.println("C�digo '" + s + "': n�o encontrado mas prefixo!!!");
			else
				System.out.println("C�digo '" + s + "' corresponde � posi��o " + pos + " do alfabeto");
		}
						
		return pos;
	}	
	
	//procura c�digo na �rvore a partir da ra�z (representado por String com 0s e 1s
	public int findNode(String s, boolean verbose)
	{
		return findNode(s, root, verbose);
	}
	
	//actualiza n� corrente na �rvore com base no n� actual e n� pr�ximo bit
	//devolve -1 se n�o encontrou o n�, -2 se encontrou mas n�o � folha, index se � folha
	public int nextNode(char c)
	{
		int pos;
		
		if (curNode.isLeaf())
			pos = -1;
		
		if (c == '0')
			if (curNode.left != null)
			{
				curNode = curNode.left;
				if (curNode.isLeaf())
					pos = curNode.index;
				else
					pos = -2;
			}
			else
				pos = -1;
		else // c == 1
			if (curNode.right != null)
			{
				curNode = curNode.right;
				if (curNode.isLeaf())
					pos = curNode.index;
				else
					pos = -2;
			}
			else
				pos = -1;								
		
		return pos;
	}
}


/*------------------------------------------------------------------------------*/

class HFNode
{
	int index = -1;  //se folha, guarda posi��o no alfabeto; sen�o, -1;
	int level = 0; // n�vel do n� na �rvore
	HFNode left, right;  //refer�ncias para os filhos direito e esquerdo: � folha se ambos forem null	
	
	HFNode (int i, int lv)
	{
		this(i, lv, null, null);
	}
	
	HFNode (int i, int lv, HFNode l, HFNode r)
	{
		index = i;
		level = lv;
		left = l;
		right = r;
	}
	
	
	//verifica se o n� � folha
	boolean isLeaf()
	{
		if (left == null && right == null)
			return true;
		else
			return false;
	}	
}