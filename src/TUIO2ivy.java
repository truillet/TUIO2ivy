package fr.irit.ihcs.gateway;
/*
	TUIO Java backend - part of the reacTIVision project
	http://reactivision.sourceforge.net/
	
	Copyright (c) 2005-2008 Martin Kaltenbrunner <mkalten@iua.upf.edu>

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

import com.illposed.osc.*;
import fr.dgac.ivy.*;
import java.util.*;

public class TUIO2ivy implements OSCListener {
	
	private int port = 3333;
	private String ivy_address = "127.255.255.255:2010";
	
	private OSCPortIn oscPort;
	private Ivy bus;
	
	private int maxFingerID = -1;
	
	private long currentFrame = 0;
	private long lastFrame = 0;
	private long startTime = 0;
	private long lastTime = 0;
	
	private final int UNDEFINED = -1;
	
	public TUIO2ivy(String port, String address) {
		this.port = Integer.parseInt(port);
		this.ivy_address = address;
	}
	
	public TUIO2ivy() {
	
	}
	
	public void connect() {
		try {
			// ivy
		  bus=new Ivy("TUIO2ivy","TUIO2ivy ready",null);
      try
      {
        bus.start(this.ivy_address);
      }
		 catch(IvyException e){System.out.println("--> ERROR : Failed ton connect to ivy address "+e);}

			
			// OSC
			oscPort = new OSCPortIn(port);
			oscPort.addListener("/tuio/2Dobj",this);
			oscPort.addListener("/tuio/2Dcur",this);
			
			oscPort.startListening();
			startTime = System.currentTimeMillis();
		} catch (Exception e) {
			System.out.println("--> ERROR : Failed to connect to OSC port "+port);
		}
	}
	
	public void disconnect() {
		oscPort.stopListening();
		try { Thread.sleep(100); }
		catch (Exception e) {};
		oscPort.close();
  	bus.stop();
	}

	public void acceptMessage(Date date, OSCMessage message) {
	
		Object[] args = message.getArguments();
		String command = (String)args[0];
		String address = message.getAddress();
    
		// System.out.println("ADRESSE : " + address);
		
		if (address.equals("/tuio/2Dobj")) {

			if (command.equals("set")) {
				if ((currentFrame<lastFrame) && (currentFrame>0)) return;
				long s_id  = ((Integer)args[1]).longValue();
				int f_id  = ((Integer)args[2]).intValue();
				float xpos = ((Float)args[3]).floatValue();
				float ypos = ((Float)args[4]).floatValue();
				float angle = ((Float)args[5]).floatValue();
				float xspeed = ((Float)args[6]).floatValue();
				float yspeed = ((Float)args[7]).floatValue();
				float rspeed = ((Float)args[8]).floatValue();
				float maccel = ((Float)args[9]).floatValue();
				float raccel = ((Float)args[10]).floatValue();
				
				System.out.println("set obj " +s_id+" "+f_id+" "+xpos+" "+ypos+" "+angle+" "+xspeed+" "+yspeed+" "+rspeed+" "+maccel+" "+raccel);
				try {
          // -- tout n'est pas complet -- 
          bus.sendMsg("TUIO2ivy command=set type=object id="+s_id + " angle="+ angle +" x="+xpos+ " y="+ypos+ " vx="+xspeed +" vy="+yspeed+ " a="+maccel); 
				}
				catch (IvyException ie)
				{
					System.out.println("--> ivy ERROR: can't send my message !");
				}
				
			} 
			else if (command.equals("alive")) {
				if ((currentFrame<lastFrame) && (currentFrame>0)) return;
        // liste des sessions alive
        for (int i=1; i<args.length; i++) {  
          System.out.println("\talive id: " + args[i].toString());
          try {
            bus.sendMsg("TUIO2ivy command=alive id="+args[i].toString()); 
          }
          catch (IvyException ie)
          {
            System.out.println("--> ivy ERROR: can't send my message !");
          } 		
        }  
			} 
			
			else if (command.equals("fseq")) {
				if (currentFrame>=0) lastFrame = currentFrame;
				currentFrame = ((Integer)args[1]).intValue();
				
				if ((currentFrame>=lastFrame) || (currentFrame<0)) {
					
					long currentTime = lastTime;
					if (currentFrame>lastFrame) {
						currentTime = System.currentTimeMillis()-startTime;
						lastTime = currentTime;
					}
				}
			}

		} else if (address.equals("/tuio/2Dcur")) {

			if (command.equals("set")) {
				if ((currentFrame<lastFrame) && (currentFrame>0)) return;

				long s_id  = ((Integer)args[1]).longValue();
				float xpos = ((Float)args[2]).floatValue();
				float ypos = ((Float)args[3]).floatValue();
				float xspeed = ((Float)args[4]).floatValue();
				float yspeed = ((Float)args[5]).floatValue();
				float maccel = ((Float)args[6]).floatValue();
				
				System.out.println("set cur " + s_id+" "+xpos+" "+ypos+" "+xspeed+" "+yspeed+" "+maccel);
				try {
          bus.sendMsg("TUIO2ivy command=set type=cursor id="+s_id+" x="+xpos+ " y="+ypos+ " vx="+xspeed +" vy="+yspeed+ " a="+maccel); 
				}
				catch (IvyException ie)
				{
					System.out.println("--> ivy ERROR: can't send my message !");
				}				
			} 
			else if (command.equals("alive")) {
				if ((currentFrame<lastFrame) && (currentFrame>0)) return;
	
        // liste des sessions alive
        for (int i=1; i < args.length;i++) {  
          System.out.println("\talive id: " + args[i].toString());
          try {
            bus.sendMsg("TUIO2ivy command=alive id="+args[i].toString()); 
          }
          catch (IvyException ie)
          {
            System.out.println("--> ivy ERROR: can't send my message !");
          } 				
        }  
			}

 		} 

			else if (command.equals("fseq"))
			{

				if (currentFrame>=0) lastFrame = currentFrame;
				currentFrame = ((Integer)args[1]).intValue();
				
				try {
            bus.sendMsg("TUIO2ivy command=fseq frame="+currentFrame); 
          }
          catch (IvyException ie)
          {
            System.out.println("--> ivy ERROR: can't send my message !");
          } 		
   
          
				if ((currentFrame>=lastFrame) || (currentFrame<0))
				{
					long currentTime = lastTime;
					if (currentFrame>lastFrame)
					{
						currentTime = System.currentTimeMillis()-startTime;
						lastTime = currentTime;
					}
			  } 
		  }
	 }
	
	public static void main(String argv[]) {
		
		TUIO2ivy client = null;
 
		switch (argv.length) {
			case 2:
			  try { 
          client = new TUIO2ivy(argv[0], argv[1]); }
				catch (Exception e) {
				}
				break;

			default:
          client = new TUIO2ivy("3333", "127.255.255.255:2010");
				break;
		}

		if (client!=null) {
			client.connect();
		} else {
			System.out.println("usage: java TUIO2ivy [OSC port] [ivy address:port]");
			System.exit(0);
		}
	}
		
}
