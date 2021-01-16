//********************************************************************
//  BOWCoreRule.java
//    Simulation of Book of War core mechanic.
//  Copyright (c) 2011 Daniel R. Collins. All rights reserved.
//  See the bottom of this file for any licensing information.
//********************************************************************

class BOWCoreRule {

	//-----------------------------------------------------------------
	//  Simulation constants
	//-----------------------------------------------------------------
	final int ATK_LEVEL = 0;              // Fighting ability of attacker
	final int HD_SIDES = 6;               // Hit dice sides
	final int DD_SIDES = 6;               // Damage dice sides
	final int DAMAGE_DICE = 1;            // Number of damage dice
	final int FRONT_LINE = 5;             // Men in front line of figure
	final int FIGURE_MEN = 10;            // Total men in figure
	final int NUM_ROUNDS = 100000;        // Rounds of combat
	final boolean SHOW_D6_TARGET = true;  // Convert kills to d6 target

	//-----------------------------------------------------------------
	//  Man class
	//-----------------------------------------------------------------
	static class Man {
		int hp;
		void takeDamage (int dam) { hp -= dam; }
		boolean isDead () { return hp <= 0; }			

		Man (int HD, int sides) { 
			hp = 0;
			for (int i = 0; i < HD; i++) {
				hp += (int) (Math.random() * sides) + 1;
			}
		}
	}

	//-----------------------------------------------------------------
	//  Die roll (single)
	//-----------------------------------------------------------------
	int die (int sides) {
		return (int) (Math.random() * sides) + 1;
	}

	//-----------------------------------------------------------------
	//  Roll damage for one hit
	//-----------------------------------------------------------------
	int rollDamage () {
		int damage = 0;
		for (int i = 0; i < DAMAGE_DICE; i++) {
			damage += die(DD_SIDES);
		}
		return damage;	
	}

	//-----------------------------------------------------------------
	//  One attack on a given man
	//    Returns true if killed
	//-----------------------------------------------------------------
	void oneAttack (int HD, int AC, Man man) {
		if (die(20) + AC + ATK_LEVEL >= 20) {
			man.takeDamage(rollDamage());
		}
	}

	//-----------------------------------------------------------------
	//  One round of combat (returns men killed)
	//    Each man in front row is attacked once
	//-----------------------------------------------------------------
	int oneRound (int HD, int AC, Man[] front) {
		int killed = 0;
		for (int i = 0; i < front.length; i++) {
			oneAttack(HD, AC, front[i]);
			if (front[i].isDead()) {
				front[i] = new Man(HD, HD_SIDES);
				killed++;
			}
		}
		return killed;
	}

	//-----------------------------------------------------------------
	//  One battle (returns men killed)
	//-----------------------------------------------------------------
	int oneBattle (int HD, int AC) {

		// Init front line
		Man[] front = new Man[FRONT_LINE];
		for (int i = 0; i < FRONT_LINE; i++) {
			front[i] = new Man(HD, HD_SIDES);		
		}
	
		// Combat loop
		int killed = 0;
		for (int i = 0; i < NUM_ROUNDS; i++) {
			killed += oneRound(HD, AC, front);			
		}
		return killed;
	}

	//-----------------------------------------------------------------
	//  Mean figure kills (per BOW turn)
	//-----------------------------------------------------------------
	double meanFigureKills (int killed, int roundsPerTurn) {
		return (double) killed / FIGURE_MEN / NUM_ROUNDS * roundsPerTurn;
	}

	//-----------------------------------------------------------------
	//  Mean figure hits (per BOW turn)
	//    Pro-rates kills per HD (i.e., 10 RPG HD worth)
	//-----------------------------------------------------------------
	double meanFigureHits (int killed, int roundsPerTurn, int HD) {
		return meanFigureKills(killed, roundsPerTurn) * HD;
	}

	//-----------------------------------------------------------------
	//  d6Target
	//    Converts a probability to a target on d6
	//-----------------------------------------------------------------
	int d6Target (double p) {
		return (int) (7 - 6 * p + 0.5);
	}

	//-----------------------------------------------------------------
	//  Print function
	//-----------------------------------------------------------------
	void printf (String s) {
		System.out.print(s);
	}

	//-----------------------------------------------------------------
	//  Make table
	//-----------------------------------------------------------------
	public void makeTable (int roundsPerTurn) {
	
		// Title
		printf("Core Mechanic @ " + roundsPerTurn + " round(s) per turn:\n\n");
  
		// Header
		printf("\t\t\tHD\nAC\t");
		for (int HD = 1; HD <= 8; HD++) {
			printf(HD + "\t");
		}
		printf("\n----------------------------\n");

		// Body
		for (int AC = 10; AC >= 1; AC -= 3) {
			printf(AC + "\t");
			for (int HD = 1; HD <= 8; HD++) {
				int battleKills = oneBattle(HD, AC);
				double hitChance = meanFigureHits(battleKills, roundsPerTurn, HD);
				if (SHOW_D6_TARGET)
					printf(d6Target(hitChance) + "\t");
				else
					printf(String.format("%.2g\t", hitChance));
			}
			printf("\n");
		}

		// Tail
		printf("\n");
	}	

	//-----------------------------------------------------------------
	//  Main method
	//-----------------------------------------------------------------
	public static void main (String[] args) {
		BOWCoreRule bcr = new BOWCoreRule();
		for (int AC = 10; AC >= 1; AC -= 3)
			bcr.oneBattle(1, AC);
		for (int rounds = 1; rounds <= 6; rounds++)
			bcr.makeTable(rounds);
	}
}

/*
=====================================================================
LICENSING INFORMATION

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA

The author may be contacted by email at: delta@superdan.net
=====================================================================
*/
