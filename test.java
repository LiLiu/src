
public class test {
	
	
	public static void main(String[] args)
	{
		String st = "1.11.1.2";
		String[] str = st.split("\\.");
		
		int count = 0;
		for(int i = 0; i < str.length; i ++)
		{
			System.out.println("value of: " + str[i]);
			
		}

		System.out.println("str.length" + str.length);
		
	}
}
