from datetime import timedelta
from base.Pattern import Pattern
from base.PatternStructure import SeqOperator, KleeneClosureOperator, PrimitiveEventStructure
from condition.CompositeCondition import AndCondition
from condition.Condition import Variable, SimpleCondition, BinaryCondition
from condition.KCCondition import KCIndexCondition
from base.Event import Event
from misc.ConsumptionPolicy import ConsumptionPolicy
from misc.SelectionStrategies import SelectionStrategies

policy = ConsumptionPolicy(
    primary_selection_strategy=SelectionStrategies.MATCH_ANY,
    secondary_selection_strategy=SelectionStrategies.MATCH_SINGLE,
    single="BikeTrip"
)


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
            getattr_func=lambda e: e,
            relation_op=lambda prev, curr: (
                prev["bike_id"] == curr["bike_id"] and
                float(prev["end_station"]) == float(curr["start_station"])
            ),
            offset=1
        )
    )

    # Final 'b': end_station in {7.0, 8.0, 9.0}
    conditions.add_atomic_condition(
        BinaryCondition(
            Variable("a", lambda xs: xs[-1]["bike_id"]),
            Variable("b", lambda x: x["bike_id"]),
            relation_op=lambda a_bike, b_bike: a_bike == b_bike
        )
    )

    conditions.add_atomic_condition(
        SimpleCondition(
            Variable("b", lambda x: float(x["end_station"])),
            relation_op=lambda s: s in {7.0, 8.0, 9.0}
        )
    )

    # Time window
    return Pattern(structure, conditions, timedelta(hours=1), consumption_policy=policy)

