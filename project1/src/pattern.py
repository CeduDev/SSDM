import sys
import os

# Make the local OpenCEP source importable
sys.path.append(os.path.join(os.path.dirname(__file__), '../..', 'OpenCEP'))

from datetime import timedelta

from base.Pattern import Pattern
from base.PatternStructure import SeqOperator, KleeneClosureOperator, PrimitiveEventStructure
from condition.CompositeCondition import AndCondition
from condition.Condition import Variable, SimpleCondition
from condition.KCCondition import KCIndexCondition


def bike_hot_path_pattern():
    # Pattern structure: SEQ( (BikeTrip a)+ , BikeTrip b ) within 1 hour
    structure = SeqOperator(
        KleeneClosureOperator(PrimitiveEventStructure("BikeTrip", "a")),
        PrimitiveEventStructure("BikeTrip", "b")
    )

    conditions = AndCondition()

    # For every consecutive pair in the Kleene closure: same bike_id
    conditions.add_atomic_condition(
        KCIndexCondition(
            names={"a"},
            getattr_func=lambda e: e.payload["bike_id"],
            relation_op=lambda x, y: x == y,
            offset=1,
        )
    )

    # For every consecutive pair in the Kleene closure: prev.end_station == curr.start_station
    conditions.add_atomic_condition(
        KCIndexCondition(
            names={"a"},
            getattr_func=lambda e: e.payload,
            relation_op=lambda prev_payload, curr_payload: prev_payload["end_station"] == curr_payload["start_station"],
            offset=1,
        )
    )

    # Final event constraint: end station is in {7,8,9}
    conditions.add_atomic_condition(
        SimpleCondition(
            Variable("b", lambda x: x["end_station"]),
            relation_op=lambda v: v in {7, 8, 9},
        )
    )

    return Pattern(structure, conditions, timedelta(hours=1))