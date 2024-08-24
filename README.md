### <img src="https://raw.githubusercontent.com/danielcranney/readme-generator/main/public/icons/skills/java-colored.svg" width="36" height="36" alt="Java" /> Programming III project

This project simulates temperature distribution on a two-dimensional metal plate using a discrete model where the plate is represented as a grid. 
The edges of the plate are fixed at 0°C, while random heat sources are set at 100°C. The simulation iteratively calculates the temperature of each 
grid element based on its neighbors until the system reaches a stable state. The program is implemented in three modes—sequential, parallel, and 
distributed—to compare performance. A graphical interface visualizes the temperature gradient in real-time, with color coding from blue (cool) to 
red (hot), although the MPI-based implementation focuses solely on computation. Extensive testing is performed to analyze runtime scalability based 
on grid size and the number of heat sources.
