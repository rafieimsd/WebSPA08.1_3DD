/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
    
package net.seleucus.wsp.server.commands;

import net.seleucus.wsp.server.WSServer;

/**
 *
 * @author masoud
 */
public class WSUsernameShow extends WSCommandOption{
    




	public WSUsernameShow(WSServer myServer) {
		super(myServer);
	}

	@Override
	protected void execute() {

		final String usernames = this.myServer.getWSDatabase().users.showUsernames();
		myServer.println(usernames);

	} // execute method

	@Override
	public boolean handle(final String cmd) {

		boolean validCommand = false;

		if(isValid(cmd)) {
			validCommand = true;
			this.execute();
		}
		
		return validCommand;
		
	} // handle method

	@Override
	protected boolean isValid(final String cmd) {
		
		boolean valid = false;
		
		if(cmd.equalsIgnoreCase("username show")) {
			
			valid = true;
		
		}
		
		return valid;
		
	}  // isValid method

}

    
    