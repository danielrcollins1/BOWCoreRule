/******************************************************************************
*  BOWCoreRule.java
*    Simulation of Book of War core mechanic.
*
*  @author   Daniel R. Collins
*  @since    2011-10-17
******************************************************************************/

class BOWCoreRule {

	//-----------------------------------------------------------------
	//  Simulation constants
	//-----------------------------------------------------------------
	static final int ATK_LEVEL = 1;              // Fighting ability
	static final int ATK_MOD = 0;                // Attack modifier
	static final int HD_SIDES = 6;               // Hit dice sides
	static final int DD_SIDES = 6;               // Damage dice sides
	static final int DAMAGE_DICE = 1;            // Number of damage dice
	static final int DAMAGE_MOD = 0;             // Damage modifier
	static final int FRONT_LINE = 5;             // Men in front line
	static final int FIGURE_MEN = 10;            // Total men in figure
	static final int NUM_ROUNDS = 100000;        // Rounds of combat
	static final boolean POISON_ATK = false;     // Poison attack form
	static final boolean SHOW_D6_TARGET = true;  // Convert kills to d6

	//-----------------------------------------------------------------
	//  Inner class
	//-----------------------------------------------------------------

	/**
	* One fighting man.
	*/
	static class Man {
		private int hp;
		void takeDamage(int dam) { hp -= dam; }
		void autoKill() { hp = 0; }
		boolean isDead() { return hp <= 0; }

		Man(int hitDice, int sides) { 
			hp = 0;
			for (int i = 0; i < hitDice; i++) {
				hp += (int) (Math.random() * sides) + 1;
			}
		}
	}

	//-----------------------------------------------------------------
	//  Class methods
	//-----------------------------------------------------------------

	/**
	* Roll a single die.
	* @param sides number of sides on the die.
	* @return the value of the die-roll.
	*/
	int rollDie(int sides) {
		return (int) (Math.random() * sides) + 1;
	}

	/**
	* Roll damage for one hit.
	* @return the damage.
	*/
	int rollDamage() {
		int damage = DAMAGE_MOD;
		for (int i = 0; i < DAMAGE_DICE; i++) {
			damage += rollDie(DD_SIDES);
		}
		return damage;	
	}

	/**
	* Make one attack on a given man.
	* @param hitDice hit dice of the target.
	* @param armorClass armor class of the target.
	* @param man the target man.
	*/
	void makeAttack(int hitDice, int armorClass, Man man) {
		if (rollDie(20) + armorClass + ATK_LEVEL + ATK_MOD >= 20) {
			man.takeDamage(rollDamage());
			if (POISON_ATK) {
				if (rollDie(20) + hitDice + 4 < 20) {
					man.autoKill();				
				}			
			}
		}
	}

	/**
	* Run one round of combat.
	* Each man in front row is attacked once.
	* @param hitDice hit dice of the targets.
	* @param armorClass armor class of the targets.
	* @param front array of men in front line.
	* @return number of men killed.
	*/
	int runRound(int hitDice, int armorClass, Man[] front) {
		int killed = 0;
		for (int i = 0; i < front.length; i++) {
			makeAttack(hitDice, armorClass, front[i]);
			if (front[i].isDead()) {
				front[i] = new Man(hitDice, HD_SIDES);
				killed++;
			}
		}
		return killed;
	}

	/**
	* Run one battle.
	* @param hitDice hit dice of targets.
	* @param armorClass armor class of targets.
	* @return number of men killed.
	*/
	int runBattle(int hitDice, int armorClass) {

		// Init front line
		Man[] front = new Man[FRONT_LINE];
		for (int i = 0; i < FRONT_LINE; i++) {
			front[i] = new Man(hitDice, HD_SIDES);
		}
	
		// Combat loop
		int killed = 0;
		for (int i = 0; i < NUM_ROUNDS; i++) {
			killed += runRound(hitDice, armorClass, front);
		}
		return killed;
	}

	/** 
	* Compute mean number of figures killed per BOW turn.
	* @param killed total number of men killed in battle.
	* @param roundsPerTurn number of rounds per turn.
	* @return mean number of figures killed per turn.
	*/
	double getMeanFigureKills(int killed, int roundsPerTurn) {
		return (double) 
			killed / FIGURE_MEN / NUM_ROUNDS * roundsPerTurn;
	}

	/**
	* Compute mean number of figure-hits per BOW turn.
	* Pro-rates kills per hit die (i.e., 10 RPG HD worth).
	* @param killed total number of men killed in battle.
	* @param roundsPerTurn number of rounds per turn.
	* @param hitDice hit dice of targets.
	* @return mean number of figure-hits per turn.
	*/
	double getMeanFigureHits(int killed, int roundsPerTurn, int hitDice) {
		return getMeanFigureKills(killed, roundsPerTurn) * hitDice;
	}

	/**
	* Convert a probability to a target on d6.
	* @param probability the success probability.
	* @return target on a d6 roll.
	*/
	int getD6Target(double probability) {
		return (int) (7 - 6 * probability + 0.5);
	}

	/**
	* Print function.
	* @param str string to print.
	*/
	void printf(String str) {
		System.out.print(str);
	}

	/**
	* Make table of simulated attack results.
	* @param roundsPerTurn number of rounds per turn.
	*/
	public void makeTable(int roundsPerTurn) {
	
		// Title
		printf("Core Mechanic @ " 
			+ roundsPerTurn + " round(s) per turn:\n\n");
  
		// Header
		printf("\t\t\tHD\nAC\t");
		for (int hitDice = 1; hitDice <= 8; hitDice++) {
			printf(hitDice + "\t");
		}
		printf("\n");

		// Body
		for (int armor = 10; armor >= 1; armor -= 3) {
			printf(armor + "");
			for (int hitDice = 1; hitDice <= 8; hitDice++) {
				int battleKills = runBattle(hitDice, armor);
				double hitChance = getMeanFigureHits(
					battleKills, roundsPerTurn, hitDice);
				printf(SHOW_D6_TARGET
					? "\t" + getD6Target(hitChance)
					: String.format("\t%.2g", hitChance));
			}
			printf("\n");
		}

		// Tail
		printf("\n");
	}	

	/**
	* Main application method.
	* @param args command-line argumrnts.
	*/
	public static void main(String[] args) {
		BOWCoreRule bcr = new BOWCoreRule();
		for (int rounds = 1; rounds <= 6; rounds++) {
			bcr.makeTable(rounds);
		}
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
