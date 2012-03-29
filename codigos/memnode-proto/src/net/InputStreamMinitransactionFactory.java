package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import node.CompareItem;
import node.Minitransaction;
import node.MinitransactionFactory;
import node.ReadItem;
import node.WriteItem;

public class InputStreamMinitransactionFactory implements MinitransactionFactory {

	private final BufferedReader reader;
	
	public InputStreamMinitransactionFactory(InputStream inputStream) {
		this.reader = new BufferedReader(new InputStreamReader(inputStream));
	}
	
	@Override
	public Minitransaction create() {
		try {
			String line = reader.readLine();
			
			if(line == null)
				return null;
			
			StringTokenizer idTokenizer = new StringTokenizer(line);
			
			if( !idTokenizer.hasMoreTokens() )
				return null;
			
			if( !"ID".equals(idTokenizer.nextToken()) )
				return null;
			
			final Minitransaction minitransaction = new Minitransaction(idTokenizer.nextToken());
			
			forread:
			for( line = reader.readLine(); line != null; line = reader.readLine() ) {
				StringTokenizer tokenizer = new StringTokenizer(line);

				while( tokenizer.hasMoreTokens() ) {
					String token = tokenizer.nextToken();
					
					if( "COMPARE".equals(token) ) {
						minitransaction.add(new CompareItem(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()), tokenizer.nextToken().getBytes()));
					}
					else if( "READ".equals(token) ) {
						minitransaction.add(new ReadItem(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken())));
					}
					else if( "WRITE".equals(token) ) {
						minitransaction.add(new WriteItem(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()), tokenizer.nextToken().getBytes()));
					}
					else if( "END".equals(token) ) {
						break forread;
					}
				}
			}
			return minitransaction;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
