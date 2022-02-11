# Complex Adaptive Systems
### Description
This is a little container and simulator for Complex Adaptive Systems (CAS).

CAS are typically living [Complex Systems](https://en.wikipedia.org/wiki/Complex_system#:~:text=Complex%20systems%20are%20systems%20whose%20behavior%20is%20intrinsically,or%20between%20a%20given%20system%20and%20its%20environment.).

If you understand italian you can read [Thesis.pdf](https://github.com/PieMH/Complex-Adaptive-Systems/blob/main/Thesis.pdf) where I explain the theory behind this work.

There are 3 differents CAS modelled in this simulator.

In order of Complexity:
<ol> 
    <li> An Ant Simulator;
    <li> A Social Game System;
    <li> The Game of Life by Conway.
</ol>

The first one is actually a CASS (A social CAS) that simulate an [ant colony](https://en.wikipedia.org/wiki/Ant).

Ants are characterized by living in a complex adaptive social system form of many ants. A single ant is pretty stupid, but many of them communicating and reacting to one another usually generates emergent behavior.
// An abstract of emergent behavior you can observe and other observations

The Social Game System modelled here is a simple CASS in which the agents of the system don't move around. They live, die, meet other agents (called acquaintances), that lives near them, to communicate with them and procreate.
The system showcase emergent behavior such as overpopulation, scarcity of food, aggregate behavior, cooperation and competition between different characters.

The [Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) modelled here used the rules of the model invented by J.H.Conway. It is a cellular automata and one of the simpler CAS you can build. Every agent of the systems runs one basic rule that simulate a tendency of equilibrium in the number of popoulation.
The system eventually lies on one equilibrium. There are three different equilibrium points. One in which no agent is alive. One in which the agents are fixed and don't move around anymore. One in which two or more agents form a pattern in which the agents die and born endlessly in cycle in the same spot. Every time you run a simulation eventually you will end up on one of this three different equilibrium point.
Note the non-linearity of the system. A single newborn or a new death can destabilize the system and lead it to a whole new state and maybe a different final equilibrium point.


### Build and Run
Build the src folder with a jdk from java 8 onward. Works best if imported in IntelliJ.

Run the simulation by running "Launcher.java".

### How to

In the Options Menu you can click on a button to toggle between the three different models.

Select which model you want to simulate than <b>REMEMBER</b> to click on Apply to see the changes.

Run the simulation with the Play button, change the speed by moving the slider to the bottom-right corner.

Every square on the grid is either full or empty. Every agent of te system at a given time occupies only one square. 
You can create new agents with a click on an empty square on the grid or kill one if you click on a full square.

Different models behave differently and have different buttons and actions. For example if you run "Social Game System" and click on Pause you can hover on a square on the grid to see the acquaintances of the agent associated with that square.