-- Sample luaj program that presents an animated Applet designed to work 
-- with the SampleApplet.java code in the examples/jse directory.
-- 
-- The Applet.init() method loads and executes this script with the Applet
-- as the first argument.
--
-- Other Applet lifecycle events are forwarded by the SampleApplet
-- by looking for these global variables and invoking them:
--   start()
--   stop()
--   paint(graphics)
--   update(graphics)
--
-- This basic applet handles key, and mouse input, has a basic animation loop, 
-- and renders double-buffered graphics including the logo image in a swing frame.
--
local applet = (...)
print('loading', applet)

-- load the logo
local logo = applet:getImage(applet:getDocumentBase(), "logo.gif")
print('logo', logo)

-- implement the applet painting, start, and stop methods.
local animate,render
local prev, interval = 0,1/60
function update(graphics)
	-- avoids clearing background
	applet:paint(graphics)
end
function paint(graphics)
	applet:repaint()
	local curr = os.time()
	local diff = curr - prev
	if diff >= interval then
		prev = curr
		pcall(animate)
		pcall(render, graphics)
	end
end
function start()
	applet:repaint()
end
function stop()
    -- do nothing
end

-- the animation step moves the line endpoints
local x1,y1,x2,y2,xi,yi = 160,240,480,240,0,0
local vx1,vy1,vx2,vy2,vxi,vyi = -5,-6,7,8,3,1
local advance = function(x,vx,max,rnd)
	x = x + vx
	if x < 0 then
		return 0, math.random(2,10)
	elseif x > max then
		return max, math.random(-10,-2)
	end
	return x, vx
end
animate = function()
	x1,y1,x2,y2 = x1+1,y1+1,x2-1,y2-1
	local w,h = applet:getWidth(), applet:getHeight()
	x1,vx1 = advance(x1,vx1,w)
	y1,vy1 = advance(y1,vy1,h)
	x2,vx2 = advance(x2,vx2,w)
	y2,vy2 = advance(y2,vy2,h)
	xi,vxi = advance(xi,vxi,w-100)
	yi,vyi = advance(yi,vyi,h-100)
end

-- the render step draws the scene
local bg = luajava.newInstance("java.awt.Color", 0x22112244,true);
local fg = luajava.newInstance("java.awt.Color", 0xffaa33);
render = function(graphics)
	graphics:setColor(bg)
	graphics:fillRect(0,0,applet:getWidth(), applet:getHeight())
	graphics:drawImage(logo,xi,yi)
	graphics:setColor(fg)
	graphics:drawLine(x1,y1,x2,y2)
end

-- add mouse listeners for specific mouse events
applet:addMouseListener(luajava.createProxy("java.awt.event.MouseListener", {
	mousePressed = function(e)
		print('mousePressed', e:getX(), e:getY(), e)
		x1,y1 = e:getX(),e:getY()
	end,
	-- mouseClicked = function(e) end, 
	-- mouseEntered = function(e) end, 
	-- mouseExited = function(e) end, 
	-- mouseReleased = function(e) end, 
}))

applet:addMouseMotionListener(luajava.createProxy("java.awt.event.MouseMotionListener", {
	mouseDragged = function(e)
		-- print('mouseDragged', e:getX(), e:getY(), e)
		x2,y2 = e:getX(),e:getY()
	end,
	-- mouseMoved= function(e) end, 
}))

-- add key listeners
applet:addKeyListener(luajava.createProxy("java.awt.event.KeyListener", {
	keyPressed = function(e) 
		local id, code, char, text = e:getID(), e:getKeyCode(), e:getKeyChar(), e:getKeyText(e:getKeyCode())
		print('key id, code, char, text, pcall(string.char,char)', id, code, char, text, pcall(string.char,char))
	end,
	-- ckeyReleased = function(e) end, 
	-- keyTyped = function(e) end, 
}))

