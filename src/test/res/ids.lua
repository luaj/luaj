-- utility to give tables and functions uniform ids for testing
ids = {}

-- return unique id for the value, independent of object id
function id(obj)
	if not obj or type(obj) == 'number' or type(obj) == 'string' or type(obj) == 'boolean' then
		return obj
	end
	local v = ids[obj]
	if v then
		return v
	end
	table.insert(ids,obj)
	ids[obj] = type(obj)..'.'..tostring(#ids)
	return ids[obj]
end 

-- return key-value pairs sorted by keys
function spairs(unsorted)
	local keys = {}
	for k,v in pairs(unsorted) do
		table.insert(keys,k)
	end
	table.sort(keys)
	local lookup = {}
	for i,k in ipairs(keys) do
		lookup[k] = i;
	end
	local iterator = function(tbl,key)
		local i = lookup[key]
		local j,k = next(keys,i)
		if not j then return nil end
		return k,unsorted[k]
	end
	return iterator, table, nil
end