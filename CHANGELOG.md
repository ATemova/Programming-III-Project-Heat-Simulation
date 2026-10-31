# Changelog

## [1.0.0] - 2026-10-31

### Changed
- One project instead of six iteration folders; earlier code stays available at tag `v0.1.0`
- Maven build producing a runnable jar, with a single CLI for every mode
- Heat sources are held at 100 °C, as the model describes, instead of fading away

### Fixed
- Distributed mode actually distributes: the master sends row bands with halo rows to RMI workers and gathers the results. Before, it never called the workers, ran a single iteration, and indexed row -1 of the grid
- Distributed workers can run as separate JVMs or machines (`worker --port`, `distributed --hosts`)
- Parallel mode no longer creates an unused thread pool; work is split across a fixed pool per iteration
- The GUI is wired into the CLI (`--gui`) for any mode, instead of living in separate classes

### Added
- `benchmark` mode: times all modes on several grid sizes, reports speed-up, writes CSV, and verifies every mode produces the same plate
- Unit tests for mode equivalence, fixed cells, seeding and band partitioning
- CI that builds, tests and attaches the jar to releases
- README with the model, run instructions and a screenshot; MIT license

### Docs
- The README previously described the distributed mode as MPI; it uses Java RMI

## [0.1.0] - 2024-08-24

Course submission for Programming III.
