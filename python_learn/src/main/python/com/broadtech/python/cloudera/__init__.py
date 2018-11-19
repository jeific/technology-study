def enum(**enums):
    """
    Function to create a python enumeration class. Create it like:
      Houses = enum(GRYFFINDOR=1, SLYTHERIN=2, HUFFLEPUFF=3, RAVENCLAW=4)
    and use it like:
      potters_house = Houses.GRYFFINDOR
    """
    return type('Enum', (), enums)
