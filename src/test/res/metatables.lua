-- The purpose of this test case is to demonstrate that
-- basic metatable operations on non-table types work.
-- i.e. that s.sub(s,...) could be used in place of string.sub(s,...)
local s = "hello"
print(s:sub(2,4))
