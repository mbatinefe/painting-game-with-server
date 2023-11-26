import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.Color;
import javax.swing.border.LineBorder;

enum PaintMode {
	Pixel, Area, Brush, Pixel3, Pixel5
};

// TODO:What if upload grid is not the same with current grid?
	// DONE: Give a warning to change current grid to upload.
// TODO:SYNC grid changes to other clients.
	// DONE
// TODO:Changing grid information does not work on MAC
	// DONE
 

public class UI extends JFrame {
	
	Socket socket;
	DatagramSocket socketUDP;
	DataInputStream in;
	DataOutputStream out;

	private JComboBox<String> gridSizeComboBox; // For implementation of grid panel
	private String selectedSize = "50x50"; // Default selected grid size
	private JPanel gridPanel; //grid panel
	
	// For scrolling option
	private JScrollPane scrollPaneRight;
	private JScrollPane scrollPaneLeft;
	
	// Other base fields on UI
	private JTextField msgField;
	private JTextArea chatArea;
	private JPanel pnlColorPicker;
	private JPanel paintPanel;
	private JToggleButton tglPen;
	private JToggleButton tglBucket;
	private JToggleButton tglPen2;
	private JToggleButton tglPen3;
	private JToggleButton tglPen4;


	// Username information
	private JTextField usernameField;
	private JPanel usernamePanel;
	private JLabel usernameLabel;
	
	// Other additional feature fields on UI
	private JButton saveButton;
	private JButton uploadButton;
	private JButton eraserButton;
	private JButton clearButton;
	
	private int pastColor;
	private PaintMode pastPaintmode;
	private static UI instance;
	private int selectedColor = -543230; // golden
	
	private String selectedPenSize = "1x1";

	// Default client number
	private int userNumber = 0;
	
	int[][] data = new int[50][50]; // pixel color data array

	int blockSize = 16;
	PaintMode paintMode = PaintMode.Pixel;
	private static boolean isEraserActive = false; // Variable to track activation state
	
	/**
	 * get the instance of UI. Singleton design pattern.
	 * 
	 * @return
	 */
	public static UI getInstance() {
		if (instance == null)
			try {
				instance = new UI();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return instance;
	}

	private void receive(DataInputStream in) {
		try {
			while (true) {
				int type = in.readInt(); // block the program here until it gets from input stream
				// if input stream is empty program will hold here.
				// That is why we need to pass this into a method.
				switch (type) {
				case 0:
					// receive text message
					receiveTextMessage(in);
					break;
				case 1:
					// receive pixel message
					receivePixelMessage(in);
					break;
				case 2:
					// receive area message
					receiveAreaMessage(in);
					break;
				case 3:
					// send past data information to other clients
					sendDataMessage(in);
					break;
				case 4:
					// get the current user ID
					receiveUserNumber(in);
					break;
				case 5:
					// receive the current grid number
					receiveGridNumber(in);
					break;
				case 6:
					// send Grid Message
					sendGridMessage(in);
				default:
					// others
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace(); // for debugging DELETE LATER
		}
	}
	
	// Receive current grid size information from the server
	private void receiveGridNumber(DataInputStream in) throws IOException {
		
		// Get the first one since col and row are same
		int m = in.readInt();
		selectedSize = m+"x"+m;

		// Create new data size and update data
        int[][]temp = new int[m][m];
    	data = temp;
    	
		paintPanel.setVisible(false);
		paintPanel.setVisible(true);
    	// Basically just f5 the board. 
    	//refreshPanel();
		
	}
  
	private void refreshPanel() {
		SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
				paintPanel.setVisible(false);
				paintPanel.setVisible(true);
	        }
			
			
		});
	}
	
	// Read the current User Number of UI
	private void receiveUserNumber(DataInputStream in) throws IOException {
		userNumber = in.readInt();
		System.out.println(userNumber);
	}
	
	// Send the First User's board information in case others join later after drawing something
	private void sendGridMessage(DataInputStream in) throws IOException {
        // Split the string using 'x' as the delimiter
        String[] parts = selectedSize.split("x");
        // Extract the first part and convert it to an integer
        int rowClear = Integer.parseInt(parts[0]);
        int colClear = rowClear;

        try {
    		// Send grid informatino as well.
    		out.writeInt(3);
    		out.writeInt(rowClear);
    		out.flush();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
     
	}
	
	// Send the First User's board information in case others join later after drawing something
	private void sendDataMessage(DataInputStream in) throws IOException {
        // Split the string using 'x' as the delimiter
        String[] parts = selectedSize.split("x");
        // Extract the first part and convert it to an integer
        int rowClear = Integer.parseInt(parts[0]);
        int colClear = rowClear;

		// Send grid informatino as well.
		out.writeInt(3);
		out.writeInt(rowClear);
		out.flush();
		
		for (int i = 0; i < rowClear; i++) {
            for (int j = 0; j < colClear; j++) {
                try {
					// Send the pixel data to the server instead of updating the screen
            		out.writeInt(1);
					out.writeInt(data[i][j]);
					out.writeInt(i);
					out.writeInt(j);
					out.flush();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace(); // REMOVE WHEN U FINISH, ONLY DEBUG
				}
               
            }
        }


	}
	
	// Receive the text informations and add to chat area
	private void receiveTextMessage(DataInputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.readInt();
		in.read(buffer, 0, len);

		String msg = new String(buffer, 0, len);
		System.out.println(msg);

		SwingUtilities.invokeLater(() -> {
			chatArea.append(msg + "\n");
		});
	}

	// Receive the pen information and add to chat area
	private void receivePixelMessage(DataInputStream in) throws IOException {
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();
		paintPixel(color, x, y);
	}
	
	// Receive the bucket information and add to the board area
	private void receiveAreaMessage(DataInputStream in) throws IOException {
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();

		paintArea(color, x, y);
	}

	/**
	 * private constructor. To create an instance of UI, call UI.getInstance()
	 * instead.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private UI() throws IOException {
		setTitle("KidPaint");

		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel, BorderLayout.CENTER);
		basePanel.setLayout(new BorderLayout(0, 0));
		
         
		// ADD USERNAME PANEL
	    usernamePanel = new JPanel();
	    getContentPane().add(usernamePanel, BorderLayout.NORTH);

	    
	    usernamePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

	    usernameLabel = new JLabel("Username:");
	    usernameField = new JTextField();
	    usernameField.setPreferredSize(new Dimension(100, 25));
	    
	    JButton changeUsernameButton = new JButton("Apply Username");
	    
	    // Add an ActionListener to changeUsernameButton
	    changeUsernameButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            // Print the entered username
	            String newUsername = usernameField.getText();
	            if (newUsername.isEmpty()) {
	                newUsername = "Guest";
	            }
	            System.out.println("Username: " + newUsername);
	            try {
	            	
	            	// If username entered, send the broadcast message
	            	sendMsg(newUsername);
	            	
	                // Show components after sending the message
	                setPanelVisibility(true);

	            } catch (IOException e1) {
	                // Print the exception details for debugging
	                e1.printStackTrace();
	            }
	        }
	    });

	    
	    usernamePanel.add(usernameLabel);
	    usernamePanel.add(usernameField);
	    usernamePanel.add(changeUsernameButton); 
	    // END OF USERNAME PANEL


		paintPanel = new JPanel() {
			// refresh the paint panel
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2 = (Graphics2D) g; // Graphics2D provides the setRenderingHints method

				// enable anti-aliasing
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHints(rh);

				// clear the paint panel using black
				g2.setColor(Color.black);
				g2.fillRect(0, 0, this.getWidth(), this.getHeight());

				// draw and fill circles with the specific colors stored in the data array
				for (int x = 0; x < data.length; x++) {
					for (int y = 0; y < data[0].length; y++) {
						g2.setColor(new Color(data[x][y]));
						g2.fillArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
						g2.setColor(Color.darkGray);
						g2.drawArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
					}
				}
			}
		};

		paintPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// click then release
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// click but dont release
			}

			// handle the mouse-up event of the paint panel
			@Override
			public void mouseReleased(MouseEvent e) {
				if (paintMode == PaintMode.Area && e.getX() >= 0 && e.getY() >= 0)

					try {
						// Send the area data to the server instead of updating the screen
						out.writeInt(2);
						out.writeInt(selectedColor);
						out.writeInt(e.getX() / blockSize);
						out.writeInt(e.getY() / blockSize);
						out.flush();
					} catch(IOException e2) {
						e2.printStackTrace();
					}
			}
		});

		// panel is the drawing area
		// how we move the mouse
		paintPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (paintMode == PaintMode.Pixel && e.getX() >= 0 && e.getY() >= 0)
					// paintPixel(e.getX() / blockSize, e.getY() / blockSize);
					try {
						// Send the pixel data to the server instead of updating the screen
						out.writeInt(1);
						out.writeInt(selectedColor);
						out.writeInt(e.getX() / blockSize);
						out.writeInt(e.getY() / blockSize);
						out.flush();

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace(); // REMOVE WHEN U FINISH, ONLY DEBUG
					}
				if (paintMode == PaintMode.Brush && e.getX() >= 0 && e.getY() >= 0)
					// paintPixel(e.getX() / blockSize, e.getY() / blockSize);
					try {
						System.out.println(e.getX() + "X" + e.getY());
						

						for(int i = -1; i <= 1; i++) {
							for(int j = -1; j <= 1; j++) {
								if(((e.getY()+j*blockSize) >= 0) && ((e.getX()+j*blockSize) >= 0)) {
									out.writeInt(1);
									out.writeInt(selectedColor);
									out.writeInt((e.getX()+j*blockSize)/blockSize);
									out.writeInt((e.getY()+j*blockSize)/blockSize);
									out.flush();
								}

								
							}
						}


					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace(); // REMOVE WHEN U FINISH, ONLY DEBUG
					}
				if (paintMode == PaintMode.Pixel3 && e.getX() >= 0 && e.getY() >= 0)
					// paintPixel(e.getX() / blockSize, e.getY() / blockSize);
					try {
						System.out.println(e.getX() + "X" + e.getY());
						
						int pixel3Radius = 3;
						

						for(int i = -pixel3Radius; i <= pixel3Radius; i++) {
							for(int j = -pixel3Radius; j <= pixel3Radius; j++) {
								if(i*i + j*j <= pixel3Radius) {
									if(((e.getY()+j*blockSize) >= 0) && ((e.getX()+i*blockSize) >= 0)) {
										out.writeInt(1);
										out.writeInt(selectedColor);
										out.writeInt((e.getX()+i*blockSize)/blockSize);
										out.writeInt((e.getY()+j*blockSize)/blockSize);
										out.flush();
									}
								}

							}
						}
					


					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace(); // REMOVE WHEN U FINISH, ONLY DEBUG
					}
				if (paintMode == PaintMode.Pixel5 && e.getX() >= 0 && e.getY() >= 0)
					// paintPixel(e.getX() / blockSize, e.getY() / blockSize);
					try {
						System.out.println(e.getX() + "X" + e.getY());
						
						int pixel5Radius = 5;
						
						for(int i = -pixel5Radius; i <= pixel5Radius; i++) {
							for(int j = -pixel5Radius; j <= pixel5Radius; j++) {
								if(i*i + j*j <= pixel5Radius) {
									if(((e.getY()+j*blockSize) >= 0) && ((e.getX()+i*blockSize) >= 0)) {
										out.writeInt(1);
										out.writeInt(selectedColor);
										out.writeInt((e.getX()+i*blockSize)/blockSize);
										out.writeInt((e.getY()+j*blockSize)/blockSize);
										out.flush();
									}
								}

							}
						}
					


					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace(); // REMOVE WHEN U FINISH, ONLY DEBUG
					}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		});
		
	
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));

		scrollPaneLeft = new JScrollPane(paintPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		basePanel.add(scrollPaneLeft, BorderLayout.CENTER);

		JPanel toolPanel = new JPanel();
		basePanel.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

	    // START SAVE BUTTON
	    saveButton = new JButton("Save");
	    toolPanel.add(saveButton);
        // Add a Save button listener
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveImageData();
            }
        });
        // END SAVE BUTTON
	    
        // START UPLOAD BUTTON
        uploadButton = new JButton("Upload");
        toolPanel.add(uploadButton);
        
        // Add an Upload button listener
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadImageData();
            }
        });
        // END UPLOAD BUTTON
        
        // START CLEAR BUTTON
        clearButton = new JButton("Clear");
        toolPanel.add(clearButton);

        // Add a Save button listener with a different logic
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the paint?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    clearPaint();
                }
            }
        });
        // END CLEAR BUTTON
        
        // START ERASE BUTTON
        
        // Load the eraser image
        BufferedImage originalImage = ImageIO.read(new File("eraser2.png"));

        // Resize the image
        int newWidth = 24;
        int newHeight = 24;
        Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // Create icon from resized image
        ImageIcon eraserIcon = new ImageIcon(resizedImage);

        // Create a button with the eraser image
        eraserButton = new JButton(eraserIcon);
        eraserButton.setPreferredSize(new Dimension(newWidth, newHeight));
        eraserButton.setBorder(new LineBorder(Color.BLACK));
		
        eraserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Toggle activation state
                isEraserActive = !isEraserActive;

                if (isEraserActive) {
                	// Add border line to indicate eraser is active
                    eraserButton.setBorder(new LineBorder(Color.RED, 3)); 
                    
                    // Store previous color and paintMode
                    pastColor = selectedColor;
                    pastPaintmode = paintMode;
                    
                    // Make it invisible because does not make sense to use when eraser is active
                    pnlColorPicker.setVisible(false);
                    tglBucket.setVisible(false);
                    tglPen2.setVisible(false);
                    tglPen3.setVisible(false);
                    tglPen4.setVisible(false);
                    paintMode = PaintMode.Pixel;
                    selectedColor = 0; // Make it black
                } else {
                    // Eraser is not active
                	
                	// Change border back to normal
                    eraserButton.setBorder(new LineBorder(Color.BLACK));
                    
                    // Activate the ones made it not visible
                    pnlColorPicker.setVisible(true);
                    tglBucket.setVisible(true);
                    tglPen2.setVisible(true);
                    tglPen3.setVisible(true);
                    tglPen4.setVisible(true);
                    // Change back to previous settings
                    selectedColor = pastColor;
                    paintMode = pastPaintmode;

                }

                // Perform actions when the button is clicked
                System.out.println("Eraser button clicked! Active: " + isEraserActive);
            }
        });
		
        // Add the clearPanel to toolPanel
        toolPanel.add(eraserButton);

        // END ERASE BUTTON
        
		pnlColorPicker = new JPanel();
		pnlColorPicker.setPreferredSize(new Dimension(24, 24));
		pnlColorPicker.setBackground(new Color(selectedColor));
		pnlColorPicker.setBorder(new LineBorder(new Color(0, 0, 0)));

		// show the color picker
		pnlColorPicker.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				ColorPicker picker = ColorPicker.getInstance(UI.instance);
				Point location = pnlColorPicker.getLocationOnScreen();
				location.y += pnlColorPicker.getHeight();
				picker.setLocation(location);
				picker.setVisible(true);
			}

		});

		toolPanel.add(pnlColorPicker);

		tglPen = new JToggleButton("Pen");
		tglPen.setSelected(true);
		toolPanel.add(tglPen);
		
		tglPen3 = new JToggleButton("Pen(3px)");
		tglPen3.setSelected(false);
		toolPanel.add(tglPen3);
		
		tglPen4 = new JToggleButton("Pen(5px)");
		tglPen4.setSelected(false);
		toolPanel.add(tglPen4);
		
		tglPen2 = new JToggleButton("Brush");
		tglPen2.setSelected(false);
		toolPanel.add(tglPen2);
		
		tglBucket = new JToggleButton("Bucket");
		toolPanel.add(tglBucket);
		
		// change the paint mode to BRUSH mode
		tglPen3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen3.setSelected(true);
				tglPen2.setSelected(false);
				tglPen4.setSelected(false);
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				paintMode = PaintMode.Pixel3;
			}
		});
		
		// change the paint mode to BRUSH mode
		tglPen4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen4.setSelected(true);
				tglPen3.setSelected(false);
				tglPen2.setSelected(false);
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				paintMode = PaintMode.Pixel5;
			}
		});


		
		// change the paint mode to BRUSH mode
		tglPen2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen2.setSelected(true);
				tglPen3.setSelected(false);
				tglPen4.setSelected(false);
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				paintMode = PaintMode.Brush;
			}
		});
		
		// change the paint mode to PIXEL mode
		tglPen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen2.setSelected(false);
				tglPen3.setSelected(false);
				tglPen4.setSelected(false);
				tglPen.setSelected(true);
				tglBucket.setSelected(false);
				paintMode = PaintMode.Pixel;
			}
		});

		// change the paint mode to AREA mode
		tglBucket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen2.setSelected(false);
				tglPen.setSelected(false);
				tglPen3.setSelected(false);
				tglPen4.setSelected(false);
				tglBucket.setSelected(true);
				paintMode = PaintMode.Area;
			}
		});
		
    	// ADD GRID
 		gridPanel = new JPanel();	    
 	    // Dropdown button with grid size options
         String[] gridSizeOptions = generateGridSizeOptions(3, 50);
         gridSizeComboBox = new JComboBox<>(gridSizeOptions);
         gridPanel.add(gridSizeComboBox);
         
  	    toolPanel.add(gridPanel, BorderLayout.NORTH);

  	// Detect changes in selection
  	  gridSizeComboBox.addItemListener(new ItemListener() {
  	      @Override
  	      public void itemStateChanged(ItemEvent e) {
  	          if (e.getStateChange() == ItemEvent.SELECTED) {
  	              selectedSize = (String) gridSizeComboBox.getSelectedItem();
  	              // Split the string using 'x' as the delimiter
  	              String[] parts = selectedSize.split("x");
  	              // Extract the first part and convert it to an integer
  	              int xx = Integer.parseInt(parts[0]);
  	              int yy = xx;

  	              // Ask the user for confirmation
  	              int response = JOptionPane.showConfirmDialog(null,
  	                      "Are you sure you want to change the grid size? This will clear the painting.",
  	                      "Confirmation", JOptionPane.YES_NO_OPTION);

  	              if (response == JOptionPane.YES_OPTION) {
  	                  // Update the data according to the new grid information
  	                  System.out.println("Selected size: " + xx + "x" + yy);
  	                  int[][] temp = new int[xx][yy];
  	                  data = temp;

  	                  try {
  	                      // Send it to the server
  	                      out.writeInt(3);
  	                      out.writeInt(xx);
  	                      out.flush();
  	                  } catch (IOException e1) {
  	                      e1.printStackTrace();
  	                  }
  	              } else {
  	                  // If the user clicks 'NO', reset the combo box to the previous selection
  	                  gridSizeComboBox.setSelectedItem(e.getItem());
  	              }
  	          }
  	      }
  	  });
	    

		JPanel msgPanel = new JPanel();

		getContentPane().add(msgPanel, BorderLayout.EAST);

		msgPanel.setLayout(new BorderLayout(0, 0));

		msgField = new JTextField(); // text field for inputting message

		msgPanel.add(msgField, BorderLayout.SOUTH);

		// handle key-input event of the message field
		msgField.addKeyListener(new KeyListener() { // text field allow interaction
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) { // if the user press ENTER
					onTextInputted(msgField.getText());
					msgField.setText("");
				}
			}

		});

		chatArea = new JTextArea(); // the read only text area for showing messages
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);

		scrollPaneRight = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneRight.setPreferredSize(new Dimension(300, this.getHeight()));
		msgPanel.add(scrollPaneRight, BorderLayout.CENTER);

        // Hide components before sending the broadcast message and connection
        setPanelVisibility(false);
        
		this.setSize(new Dimension(1100, 600));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * it will be invoked if the user selected the specific color through the color
	 * picker
	 * 
	 * @param colorValue - the selected color
	 */
	public void selectColor(int colorValue) {
		SwingUtilities.invokeLater(() -> {
			selectedColor = colorValue;
			pnlColorPicker.setBackground(new Color(colorValue));
		});
	}

	// Clear the whole painting by writing black to everywhere of data
    private void clearPaint() {
        // Split the string using 'x' as the delimiter
        String[] parts = selectedSize.split("x");
        // Extract the first part and convert it to an integer
        int rowClear = Integer.parseInt(parts[0]);
        int colClear = rowClear;
        
        for (int i = 0; i < rowClear; i++) {
            for (int j = 0; j < colClear; j++) {
                data[i][j] = 0;
                paintPanel.repaint();
                try {
					// Send the pixel data to the server instead of updating the screen
					out.writeInt(1);
					out.writeInt(0);
					out.writeInt(i);
					out.writeInt(j);
					out.flush();

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
               
            }
        }
    	paintPanel.repaint();
    	
    }
    
    // Different logic for saving image data
    private void saveImageData() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileChooser.getSelectedFile()))) {
                
                // Display grid size information before saving
                System.out.println("Saving image data with grid size information...");

                // Write the grid size to the file
                dos.writeInt(data.length);
                dos.writeInt(data[0].length);
                
            	// Serialize and write the pixel data array
                writeIntArray2D(dos, data);
                // Write the block size
                dos.writeInt(blockSize);
                // Write the selected color
                dos.writeInt(selectedColor);
                // Write the paint mode
                dos.writeUTF(paintMode.name());

                System.out.println("Image data saved successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Error saving image data.");
            }
        }
    }
    
    // Upload image data
    private void uploadImageData() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(fileChooser.getSelectedFile()))) {
                
                // Read and display the grid size information
                int rowClear = dis.readInt();
                int colClear = dis.readInt();
                System.out.println("Grid Size: " + rowClear + "x" + colClear);
                
                // Split the string using 'x' as the delimiter
                String[] parts = selectedSize.split("x");
                // Extract the first part and convert it to an integer
                int rowCurrent = Integer.parseInt(parts[0]);
               
                // give warning
                if(rowClear == rowCurrent) {	
	            	// Deserialize and read the pixel data array
	                int[][] dataUpload = readIntArray2D(dis);
	                // Split the string using 'x' as the delimiter
	
	            	data = dataUpload;
	                for (int i = 0; i < rowClear; i++) {
	                    for (int j = 0; j < colClear; j++) {
	                        paintPanel.repaint();
	                        try {
	        					// Send the pixel data to the server instead of updating the screen
	        					out.writeInt(1);
	        					out.writeInt(data[i][j]);
	        					out.writeInt(i);
	        					out.writeInt(j);
	        					out.flush();
	
	        				} catch (IOException e1) {
	        					// TODO Auto-generated catch block
	        					e1.printStackTrace();
	        				}
	                       
	                    }
	                }
                } else {
                	// Show a warning dialog if current grid size does not match with file's grid size
                    JOptionPane.showMessageDialog(null, "Warning: The grid size does not match the current ("+selectedSize +") grid size. \t"
                    		+ "Please change it to " + rowClear + "x"+rowClear+".", "Warning", JOptionPane.WARNING_MESSAGE);
                }

                System.out.println("Image data uploaded successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Error uploading image data.");
            }
        }
    }

    // Write a 2D integer array to DataOutputStream
    private void writeIntArray2D(DataOutputStream dos, int[][] array) throws IOException {
        for (int[] row : array) {
            for (int value : row) {
                dos.writeInt(value);
            }
        }
    }

    // Read a 2D integer array from DataInputStream
    private int[][] readIntArray2D(DataInputStream dis) throws IOException {
        // Split the string using 'x' as the delimiter
        String[] parts = selectedSize.split("x");
        // Extract the first part and convert it to an integer
        int rowClear = Integer.parseInt(parts[0]);
        int colClear = rowClear;
        int[][] array = new int[rowClear][colClear];

        for (int i = 0; i < rowClear; i++) {
            for (int j = 0; j < colClear; j++) {
                array[i][j] = dis.readInt();
            }
        }

        return array;
    }

	/**
	 * it will be invoked if the user inputted text in the message field
	 * 
	 * @param text - user inputted text
	 * @throws IOException
	 */
	private void onTextInputted(String text) {

		try {

			out.writeInt(0);// 0 means this is a chat message

			String username = usernameField.getText();
			String messageToSend;

			if(username.isEmpty()){
		        messageToSend = "Guest: " + text;

			} else {
		        messageToSend = username + ": " + text;
			}

			out.writeInt(messageToSend.length());
			out.write(messageToSend.getBytes());
			out.flush();

		} catch (IOException ex) {

		}
	}

	/**
	 * change the color of a specific pixel
	 * 
	 * @param col, row - the position of the selected pixel
	 */
	public void paintPixel(int col, int row) {
		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = selectedColor;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
	}

	public void paintPixel(int color, int col, int row) {
		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = color;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
	}

	/**
	 * change the color of a specific area
	 * 
	 * @param col, row - the position of the selected pixel
	 * @return a list of modified pixels
	 */
	public List paintArea(int col, int row) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;

		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();

		if (oriColor != selectedColor) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (data[x][y] != oriColor)
					continue;

				data[x][y] = selectedColor;
				filledPixels.add(p);

				if (x > 0 && data[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < data.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && data[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < data[0].length - 1 && data[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));
			}

			paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
		}
		return filledPixels;
	}
	
	public List paintArea(int color, int col, int row) {
		//System.out.println("Starting to Paint Area");
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;
		
		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();

		if (oriColor != color) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (data[x][y] != oriColor)
					continue;

				data[x][y] = color;
				filledPixels.add(p);

				if (x > 0 && data[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < data.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && data[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < data[0].length - 1 && data[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));
			}

			paintPanel.repaint();
		}
		return filledPixels;
	}
	
	/**
	 * set pixel data and block size
	 * 
	 * @param data
	 * @param blockSize
	 */
	
	private void sendMsg(String username) throws IOException {
		
		// Create UDP socket with port 12345
		socketUDP = new DatagramSocket(12345);
		
		// Destination is broadcast address
		InetAddress destination = InetAddress.getByName("255.255.255.255");
		String str = username;	// Get the username information to str

		DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), destination, 45678);
		socketUDP.send(packet);
        System.out.println("\nBroadcast message sent...\n");
        
		// Create a byte array to store the received data
		byte[] receiveData = new byte[1024];
		// Create a DatagramPacket to receive the UDP packet
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		// Receive the UDP packet (blocking call)
		socketUDP.receive(receivePacket);
		System.out.println("Server details received:");

		// Extract data from the packet
        byte[] combinedData = receivePacket.getData();
        
        // Determine the size of dataA
        int dataASize = InetAddress.getByName("0.0.0.0").getAddress().length;
        
        byte[] dataA = new byte[dataASize];
        byte[] dataB = new byte[combinedData.length - dataA.length];

        System.arraycopy(combinedData, 0, dataA, 0, dataA.length);
        System.arraycopy(combinedData, dataA.length, dataB, 0, dataB.length);

        // Convert dataA to InetAddress
        InetAddress addr = InetAddress.getByAddress(dataA);

        // Convert dataB to int to get port number
        int port = Integer.parseInt(new String(dataB).trim());

        // Now you have A and B
        System.out.println("Server Address: " + addr);
        System.out.println("Server Port: " + port);

        
		// Extract the sender's IP address and port number from the received packet
		InetAddress senderAddress = addr;
		int senderPort = port;
		
		socketUDP.close(); // Close UDP connection

		try {
			// Start new connection to the server. With received server infos
			Socket socket = new Socket(senderAddress, senderPort);

			System.out.println("\nTCP Connection established.");
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			// Send every user if someone new joins to the game.
			String messageToSend = username + " joined the game.\n";
			out.writeInt(0);
			out.writeInt(messageToSend.length());
			out.write(messageToSend.getBytes());
			out.flush();
			
			// create a new thread for receiving data
			Thread t = new Thread(() -> {				
				receive(in);
			});
			t.start();
			
		 } catch (IOException e) {
	        e.printStackTrace();
	        // Handle the exception (e.g., show an error message) or throw it again if necessary
	        throw e;
	     }
	}
	
	// Add grid size options as string
	private String[] generateGridSizeOptions(int start, int end) {
	    String[] options = new String[end - start + 1];
	    for (int i = start; i <= end; i++) {
	        options[end - i] = i + "x" + i;
	    }
	    return options;
	}
	
	// Set visibility of components method before broadcast msg and after broadcast msg
	private void setPanelVisibility(boolean visible) {
	    paintPanel.setVisible(visible);
	    pnlColorPicker.setVisible(visible);
	    tglPen.setVisible(visible);
	    tglPen2.setVisible(visible);
	    tglPen3.setVisible(visible);
	    tglPen4.setVisible(visible);
	    tglBucket.setVisible(visible);
	    msgField.setVisible(visible);
	    scrollPaneLeft.setVisible(visible);
	    scrollPaneRight.setVisible(visible);

	    gridPanel.setVisible(visible);
		saveButton.setVisible(visible);
		uploadButton.setVisible(visible);
		eraserButton.setVisible(visible);
		clearButton.setVisible(visible);
		

		usernamePanel.setVisible(!visible);
	}

	
	public void setData(int[][] data, int blockSize) {
		this.data = data;
		this.blockSize = blockSize;
		
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));
		paintPanel.repaint();
	}
}
