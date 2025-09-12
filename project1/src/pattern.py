from datetime import timedelta
from base.Pattern import Pattern
from base.PatternStructure import SeqOperator, KleeneClosureOperator, PrimitiveEventStructure
from condition.CompositeCondition import AndCondition
from condition.Condition import Variable, SimpleCondition
from condition.KCCondition import KCIndexCondition


def bike_hot_path_pattern():
    # Pattern: (a)+ followed by b
    structure = SeqOperator(
        KleeneClosureOperator(PrimitiveEventStructure("BikeTrip", "a")),
        PrimitiveEventStructure("BikeTrip", "b")
    )

    conditions = AndCondition()

    conditions.add_atomic_condition(
        KCIndexCondition(
            names={"a"},
            getattr_func=lambda e: e,  # full dict
            relation_op=lambda prev, curr: (
                prev["bike_id"] == curr["bike_id"] and
                float(prev["end_station"]) == float(curr["start_station"])
            ),
            offset=1
    )
)

    # Final 'b': end_station in {7.0, 8.0, 9.0}
    conditions.add_atomic_condition(
        SimpleCondition(
            Variable("b", lambda e: float(e["end_station"])),
            relation_op=lambda v: v in {7.0, 8.0, 9.0}
        )
    )

    # Time window
    return Pattern(structure, conditions, timedelta(hours=1))


def simple_bike_trip_pattern():
    structure = SeqOperator(
        PrimitiveEventStructure("BikeTrip", "a")
    )

    # Trivial condition: end_station >= 0 (always true if station is valid)
    conditions = SimpleCondition(
        Variable("a", lambda x: x["end_station"]),
        relation_op=lambda v: v == "7"
    )

    # 1-hour time window (required, arbitrary for testing)
    return Pattern(structure, conditions, timedelta(hours=1))