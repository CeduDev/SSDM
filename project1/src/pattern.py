from datetime import timedelta
from typing import List
from base.Pattern import Pattern
from base.PatternStructure import (
    SeqOperator,
    KleeneClosureOperator,
    PrimitiveEventStructure,
)
from condition.CompositeCondition import AndCondition
from condition.Condition import Variable, SimpleCondition, BinaryCondition
from condition.KCCondition import KCIndexCondition
from misc.ConsumptionPolicy import ConsumptionPolicy
from misc.SelectionStrategies import SelectionStrategies

policy = ConsumptionPolicy(
    primary_selection_strategy=SelectionStrategies.MATCH_ANY,
    # secondary_selection_strategy=SelectionStrategies.MATCH_SINGLE,
    # single="BikeTrip",
)


def bike_hot_path_pattern(end_stations: List[float] = [7.0, 8.0, 9.0]):
    # Pattern: (a)+ followed by b, i.e., a_n events followed by b, where n > 0
    structure = SeqOperator(
        KleeneClosureOperator(PrimitiveEventStructure("BikeTrip", "a")),
        PrimitiveEventStructure("BikeTrip", "b"),
    )

    conditions = AndCondition()

    # Pattern WHERE a[i+1].bike = a[i].bike AND a[i+1].start = a[i] from the project instructions
    # i.e. the end station for event a[i] is the same as the start station for event a[i+1], and
    # both events have the same bike_id
    conditions.add_atomic_condition(
        KCIndexCondition(
            names={"a"},
            getattr_func=lambda e: e,
            relation_op=lambda prev, curr: (
                prev["bike_id"] == curr["bike_id"]
                and float(prev["end_station"]) == float(curr["start_station"])
            ),
            offset=1,
        )
    )

    # Pattern WHERE a[last].bike = b.bike from the project instructions
    # i.e. the last a event has the same bike_id as event b
    conditions.add_atomic_condition(
        BinaryCondition(
            Variable("a", lambda xs: xs[-1]["bike_id"]),
            Variable("b", lambda x: x["bike_id"]),
            relation_op=lambda a_bike, b_bike: a_bike == b_bike,
        )
    )

    # The two conditions above ensure that the chain of events (a.k.a pattern) can only be for
    # the same bike along

    # Final event b has end_station in {7.0, 8.0, 9.0}
    conditions.add_atomic_condition(
        SimpleCondition(
            Variable("b", lambda x: float(x["end_station"])),
            relation_op=lambda s: s in end_stations,
        )
    )

    # Create the pattern with a time window of 1 hour
    return Pattern(structure, conditions, timedelta(hours=1), consumption_policy=policy)
