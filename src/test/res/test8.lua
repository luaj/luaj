t = {}
t[1] = "foo"
t[2] = "bah"
print(t[1])
print(t[2])
print(t[3])

t[1] = nil
t[2] = nil
print(t[1])
print(t[2])
print(t[3])
