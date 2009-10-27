	
frame = luajava.newInstance( "javax.swing.JFrame", "Texts" );
pane = luajava.newInstance( "javax.swing.JPanel" );
borderFactory = luajava.bindClass( "javax.swing.BorderFactory" )
border = borderFactory:createEmptyBorder( 30, 30, 10, 30 )
pane:setBorder( border )
label = luajava.newInstance( "javax.swing.JLabel", "This is a Label" );


layout = luajava.newInstance( "java.awt.GridLayout", 2, 2 );
pane:setLayout( layout )
pane:add( label )
pane:setBounds( 20, 30, 10, 30 )

borderLayout = luajava.bindClass( "java.awt.BorderLayout" )
frame:getContentPane():add(pane, borderLayout.CENTER )
jframe = luajava.bindClass( "javax.swing.JFrame" )
frame:setDefaultCloseOperation(jframe.EXIT_ON_CLOSE)
frame:pack()
frame:setVisible(true)

local listener = luajava.createProxy("java.awt.event.MouseListener",
					{
						mouseClicked = function(me)
							print("clicked!", me)
						end
					})

frame:addMouseListener(listener)
