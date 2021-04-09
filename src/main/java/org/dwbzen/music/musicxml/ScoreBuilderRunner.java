package org.dwbzen.music.musicxml;

import java.util.Date;

/**
 * Interactive ScoreBuilder
 * 
 * @author don_bacon
 *
 */
public class ScoreBuilderRunner {

	public static void main(String... args)  {
		String title = null;
		String instrumentName = null;
		String fileName = null;
		ScoreBuilder scoreBuilder = null;
	    String keyName = null;
	    String timeSignature = null;
	    int tempo;
	    
    	if(args.length > 0) {
    		for(int i = 0; i<args.length; i++) {
    			if(args[i].equalsIgnoreCase("-title")) {
    				title = args[++i].toLowerCase();
    			}
    			else if(args[i].equalsIgnoreCase("-instrument")) {
    				instrumentName =  args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-file")) {
    				fileName = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-key")) {
    				keyName = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-tempo")) {
 
    			}
    			else if(args[i].equalsIgnoreCase("-timesignature")) {
    				 
    			}
    		}
    		
    	}
	}
}
