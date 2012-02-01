-- Sample luaj program that presents an animated Swing window.
-- 
-- This basic application handles key, and mouse input, has a basic animation loop, 
-- and renders double-buffered graphics including the logo image in a swing frame.
--

-- bind to classes we need
local borderLayout = luajava.bindClass("java.awt.BorderLayout")
local jframe = luajava.bindClass("javax.swing.JFrame")
local bufferedImage = luajava.bindClass("java.awt.image.BufferedImage")
local swingUtilities = luajava.bindClass("javax.swing.SwingUtilities")
local thread = luajava.bindClass("java.lang.Thread")

-- set up frame, get content pane
local frame = luajava.newInstance("javax.swing.JFrame", "Sample Luaj Application");
local content = frame:getContentPane()

-- add a buffered image as content
local image = luajava.newInstance("java.awt.image.BufferedImage", 640, 480, bufferedImage.TYPE_INT_RGB)
local icon = luajava.newInstance("javax.swing.ImageIcon", image)
local label = luajava.newInstance("javax.swing.JLabel", icon)

-- add the main pane to the main content
content:add(label, borderLayout.CENTER)
frame:setDefaultCloseOperation(jframe.EXIT_ON_CLOSE)
frame:pack()

-- simple animation framework
local tick,animate,render
local tnext = 0
tick = luajava.createProxy("java.lang.Runnable", {
	run = function()
		swingUtilities:invokeLater(tick)
		if os.time()< tnext then return thread:sleep(1) end
		tnext = math.max(os.time(),tnext+1000/60)
		pcall(animate)
		pcall(render)
		label:repaint(0,0,0,640,480)
	end
})

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
	x1,vx1 = advance(x1,vx1,640)
	y1,vy1 = advance(y1,vy1,480)
	x2,vx2 = advance(x2,vx2,640)
	y2,vy2 = advance(y2,vy2,480)
	xi,vxi = advance(xi,vxi,540)
	yi,vyi = advance(yi,vyi,380)
end

-- try loading the logo image from a couple locations
local imageio = luajava.bindClass("javax.imageio.ImageIO")
local file = luajava.bindClass("java.io.File")
local loadimage = function(path)
	local s,i = pcall(imageio.read, imageio, luajava.new(file, path))
	return s and i
end
local logo = loadimage("logo.gif") or loadimage("examples/lua/logo.gif")

-- the render step draws the scene
local g = image:getGraphics()
local bg = luajava.newInstance("java.awt.Color", 0x22112244, true);
local fg = luajava.newInstance("java.awt.Color", 0xffaa33);
render = function()
	g:setColor(bg)
	g:fillRect(0,0,640,480)
	if logo then g:drawImage(logo,xi,yi) end
	g:setColor(fg)
	g:drawLine(x1,y1,x2,y2)
end

-- add mouse listeners for specific mouse events
label:addMouseListener(luajava.createProxy("java.awt.event.MouseListener", {
	mousePressed = function(e)
		--print('mousePressed', e:getX(), e:getY(), e)
		x1,y1 = e:getX(),e:getY()
	end,
	-- mouseClicked = function(e) end, 
	-- mouseEntered = function(e) end, 
	-- mouseExited = function(e) end, 
	-- mouseReleased = function(e) end, 
}))
label:addMouseMotionListener(luajava.createProxy("java.awt.event.MouseMotionListener", {
	mouseDragged = function(e)
		--print('mouseDragged', e:getX(), e:getY(), e)
		x2,y2 = e:getX(),e:getY()
	end,
	-- mouseMoved= function(e) end, 
}))

-- add key listeners
frame:addKeyListener(luajava.createProxy("java.awt.event.KeyListener", {
	keyPressed = function(e) 
		local id, code, char, text = e:getID(), e:getKeyCode(), e:getKeyChar(), e:getKeyText(e:getKeyCode())
		print('key id, code, char, text, pcall(string.char,char)', id, code, char, text, pcall(string.char,char))
	end,
	-- keyReleased = function(e) end, 
	-- keyTyped = function(e) end, 
}))

-- use the window listener to kick off animation
frame:addWindowListener(luajava.createProxy("java.awt.event.WindowListener", {
	windowOpened = function(e) 
		swingUtilities:invokeLater(tick)
	end,
	-- windowActivated = function(e) end, 
	-- windowClosed = function(e) end, 
	-- windowClosing = function(e) end, 
	-- windowDeactivated = function(e) end, 
	-- windowDeiconified = function(e) end, 
	-- windowIconified = function(e) end, 
}))

-- utility function to load an image from a file, for reference
local loadimage = function(filename)
end

-- Set window visible last to start app.
frame:setVisible(true)
