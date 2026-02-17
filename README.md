<p align="center">
  <img src="https://img.shields.io/badge/Language-Java-red?style=flat-square&logo=java" />
  <img src="https://img.shields.io/badge/Concept-Parallel%20Computing-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/Concept-Distributed%20Systems-purple?style=flat-square" />
  <img src="https://img.shields.io/badge/Model-Discrete%20Simulation-informational?style=flat-square" />
  <img src="https://img.shields.io/badge/Status-Completed-success?style=flat-square" />
</p>

# 🔥 2D Heat Distribution Simulation  
### Programming III Project  

## 📖 Project Overview

This project simulates temperature distribution on a two-dimensional metal plate using a discrete numerical model.

The plate is represented as a grid where:

- Boundary edges are fixed at **0°C**
- Random internal heat sources are initialized at **100°C**

The system iteratively updates each grid cell’s temperature based on the average of its neighboring cells until thermal equilibrium (stable state) is reached.

## ⚙️ Simulation Model

- Discrete grid-based representation  
- Iterative relaxation algorithm  
- Neighbor-based temperature averaging  
- Convergence detection for stable state  

This approach models heat diffusion using numerical approximation techniques commonly applied in computational physics.

## 🛠️ Implementation Modes

The program is implemented in three execution modes to analyze performance and scalability:

### 1️⃣ Sequential Mode
- Single-threaded execution  
- Baseline performance measurement  

### 2️⃣ Parallel Mode
- Multi-threaded implementation  
- Shared-memory parallelism  
- Performance comparison against sequential version  

### 3️⃣ Distributed Mode (MPI)
- Message Passing Interface (MPI)  
- Distributed computation across multiple processes  
- Focused purely on computation (no GUI rendering)  

## 🎨 Visualization

A graphical user interface (GUI) visualizes the temperature distribution in real time:

- 🔵 Blue → Low temperature  
- 🔴 Red → High temperature  

The GUI dynamically updates to show heat diffusion progression across the grid.

(Note: The MPI implementation focuses solely on computational performance without graphical visualization.)

## 📊 Performance Analysis

Extensive testing was conducted to evaluate:

- Runtime scalability  
- Impact of grid size  
- Influence of number of heat sources  
- Performance differences between sequential, parallel, and distributed execution  

This allows clear comparison of computational efficiency across different execution models.

## 🎯 Learning Outcomes

- Understanding numerical simulation techniques  
- Applying parallel and distributed computing concepts  
- Performance benchmarking and scalability analysis  
- Implementing real-time visualization of scientific models  

This project combines computational physics, concurrency, and distributed systems principles into a performance-oriented simulation study.
