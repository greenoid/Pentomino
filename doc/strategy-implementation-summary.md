# Computer Strategy Implementation Summary

## Overview

This document summarizes the design for two new advanced computer strategies for the Pentomino game:

1. **ComputerStrategyMinMax**: Uses minimax algorithm with alpha-beta pruning for optimal tactical play
2. **ComputerStrategyComposite**: Combines OpenSpace strategy for early game with MinMax for endgame

## Key Design Decisions

### 1. MinMax Strategy

**Algorithm**: Minimax with alpha-beta pruning
- **Default depth**: 3 levels (computer → human → computer)
- **Heuristic**: Mobility-based (count of legal moves difference)
- **Performance target**: <10 seconds per move at depth 3

**Why this approach?**
- Minimax is proven optimal for zero-sum games
- Alpha-beta pruning reduces search space significantly
- Mobility heuristic directly relates to game objective (force opponent to lose moves)
- Depth 3 balances tactical strength with reasonable computation time

### 2. Composite Strategy

**Approach**: Two-phase strategy switching
- **Early game**: ComputerStrategyOpenSpace (fast, board control)
- **Endgame**: ComputerStrategyMinMax (optimal, tactical)
- **Switch threshold**: 28 empty squares remaining (~half board)

**Why this approach?**
- OpenSpace is fast and effective for open positions
- MinMax is essential when game tree is smaller (endgame)
- Automatic switching provides best of both worlds
- Configurable threshold allows performance tuning

### 3. Heuristic Evaluation

**Primary heuristic**: Mobility difference
```
score = (my legal moves) - (opponent legal moves)
```

**Rationale**:
- Simple and fast to compute
- Directly models game objective
- Proven effective in similar constraint-based games
- No complex tuning required

**Alternative considered**: Diffusion-based openness (from OpenSpace strategy)
- More sophisticated but slower
- Can be added as enhancement if mobility proves insufficient

### 4. Threshold Calculation

**Simple approach** (recommended initial implementation):
```
Switch when: empty_squares <= 28
```

**Calculation**:
- 8×8 board = 64 squares
- ~3 pieces remaining × 9 squares per piece ≈ 27 squares
- Threshold 28 provides buffer
- Switches around move 18-22 in typical games

**Advanced approach** (optional future enhancement):
- Use diffusion algorithm to monitor board openness
- Switch when max openness falls below threshold
- More adaptive but requires tuning

## Implementation Architecture

### Class Structure

```
ComputerStrategy (interface)
├── ComputerStrategyMinMax
│   ├── minimax() - recursive search
│   ├── evaluatePosition() - heuristic function
│   └── findAllPossibleMoves() - move generation
│
├── ComputerStrategyComposite
│   ├── earlyGameStrategy (OpenSpace)
│   ├── endGameStrategy (MinMax)
│   ├── thresholdCalculator
│   └── calculateMove() - delegates to active strategy
│
└── ThresholdCalculator (interface)
    ├── SimpleThresholdCalculator
    └── DiffusionThresholdCalculator (optional)
```

### Key Components

**ComputerStrategyMinMax**:
- Configurable search depth (1-5)
- Alpha-beta pruning for efficiency
- Mobility-based position evaluation
- Performance monitoring (time, nodes evaluated)

**ComputerStrategyComposite**:
- Delegates to two sub-strategies
- Monitors game state for switch condition
- One-time switching (no switching back)
- Logs strategy transitions

**ThresholdCalculator**:
- Interface for pluggable threshold strategies
- SimpleThresholdCalculator: counts empty squares
- DiffusionThresholdCalculator: uses openness analysis

## Performance Expectations

### ComputerStrategyMinMax
| Depth | Est. Time per Move | Strength | Use Case |
|-------|-------------------|----------|----------|
| 1 | <1 second | Weak | Testing only |
| 2 | 1-3 seconds | Moderate | Fast gameplay |
| 3 | 3-10 seconds | Strong | **Recommended** |
| 4 | 10-60 seconds | Very Strong | Analysis only |

### ComputerStrategyComposite
| Phase | Strategy | Est. Time | Moves |
|-------|----------|-----------|-------|
| Early | OpenSpace | <1 second | 1-20 |
| Switch | Transition | - | ~18-22 |
| End | MinMax (depth 3) | 3-10 seconds | 20-30+ |

## Configuration Recommendations

### For Development/Testing
```java
// Fast execution for testing logic
new ComputerStrategyMinMax(2);
new ComputerStrategyComposite(
    new ComputerStrategyOpenSpace(2),
    new ComputerStrategyMinMax(2),
    new SimpleThresholdCalculator(24)
);
```

### For Production Gameplay
```java
// Balanced strength and performance
new ComputerStrategyMinMax(3);
new ComputerStrategyComposite(
    new ComputerStrategyOpenSpace(3),
    new ComputerStrategyMinMax(3),
    new SimpleThresholdCalculator(28)
);
```

## Implementation Plan

### Phase 1: MinMax Foundation (4-6 hours)
1. Create basic MinMax class structure
2. Implement minimax recursive algorithm
3. Add mobility-based heuristic
4. Implement alpha-beta pruning

### Phase 2: Composite Strategy (3-4 hours)
1. Create ThresholdCalculator interface
2. Implement SimpleThresholdCalculator
3. Create CompositeStrategy with switching logic
4. Test strategy transitions

### Phase 3: Testing & Optimization (3-4 hours)
1. Unit tests for MinMax algorithm
2. Unit tests for Composite strategy
3. Performance benchmarking
4. Threshold tuning

### Phase 4: Documentation (1-2 hours)
1. Update strategy documentation
2. Add usage examples
3. Document configuration options

**Total estimated time**: 11-16 hours

## Risk Mitigation

### Performance Risks
**Risk**: MinMax too slow at depth 3
**Mitigation**: 
- Implement move ordering for better pruning
- Reduce depth to 2 if necessary
- Add time limit with iterative deepening

### Memory Risks
**Risk**: Out of memory during deep search
**Mitigation**:
- Limit maximum depth to 5
- Use shallow copies of GameState
- Monitor memory usage during testing

### Quality Risks
**Risk**: Heuristic doesn't play well
**Mitigation**:
- Start with simple mobility heuristic
- Test against Random and OpenSpace strategies
- Prepare to add weighted factors if needed

## Success Criteria

### Functional Requirements
- ✓ MinMax finds winning moves when available
- ✓ MinMax completes moves within 10 seconds (depth 3)
- ✓ Composite switches at appropriate game phase
- ✓ No crashes or memory errors during normal play

### Performance Requirements
- ✓ MinMax averages <8 seconds per move (depth 3)
- ✓ Composite early phase <1 second per move
- ✓ Switch occurs around move 18-22 in typical games
- ✓ Memory usage stays under 100MB

### Quality Requirements
- ✓ MinMax defeats Random strategy >90% of games
- ✓ Composite performs better than OpenSpace alone
- ✓ Code is well-documented and maintainable
- ✓ All unit tests pass

## Questions for Approval

Before proceeding to implementation, please confirm:

1. **Heuristic**: Approve mobility-based heuristic as initial implementation?
2. **Depth**: Approve depth=3 as default for MinMax?
3. **Threshold**: Approve 28 empty squares as initial threshold?
4. **Scope**: Implement SimpleThresholdCalculator first, DiffusionThresholdCalculator as Phase 4?
5. **Testing**: Prioritize unit tests, then integration tests?

## Next Steps

Once you approve this design:
1. I'll switch to **Code mode**
2. Start implementing Phase 1: MinMax Foundation
3. Test each component thoroughly
4. Proceed through phases sequentially
5. Deliver working, tested strategies

---

## Documentation References

- **Full Design**: [`doc/minmax-composite-strategy-design.md`](minmax-composite-strategy-design.md)
- **Implementation Guide**: [`doc/implementation-plan.md`](implementation-plan.md)
- **Architecture**: [`doc/architecture.md`](architecture.md)
- **OpenSpace Strategy**: [`doc/README-OpenSpaceStrategy.md`](README-OpenSpaceStrategy.md)