-- utility to give tables and functions uniform ids for testing
ids = {}
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
