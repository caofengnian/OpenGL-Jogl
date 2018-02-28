import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class VCCW03 extends Component implements KeyListener {

	private BufferedImage in, out;
	int width, height;
	File inputFile;

	public VCCW03() {
		loadImage();
		addKeyListener(this);

	}

	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	public void paint(Graphics g) {
		g.drawImage(out, 0, 0, null);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Image Processing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		VCCW03 img = new VCCW03();
		frame.add("Center", img);
		frame.pack();
		img.requestFocusInWindow();
		frame.setVisible(true);

	}

	private void processing() {
		//with reference to lab7 VC07.java
		int widthp=width+2;	//make a boundary out of the picture so 2 pixels needed to extent both in width and height
		int heightp=height+2;
		int[] rArrayred = new int[widthp*heightp];	//set each color(red, green, blue) array
		int[] rArraygreen  = new int[widthp*heightp];
		int[] rArrayblue  = new int[widthp*heightp];
		
		for (int y = 0; y < heightp; y++)
			for (int x = 0; x < widthp; x++) {
				if((y*widthp+x) < widthp || (y*widthp+x) > ((widthp*heightp)-widthp-1)) {
					rArrayred[y*widthp+x] = 255;			// set up and down boundary in black
					rArraygreen[y*widthp+x] = 255;	
					rArrayblue[y*widthp+x] = 255;
				}
				else if(((y*widthp+x) % widthp == 0) || ((y*widthp+x+1) % widthp == 0) ){
					rArrayred[y*widthp+x] = 255;			// set left and right boundary in black
					rArraygreen[y*widthp+x] = 255;
					rArrayblue[y*widthp+x] = 255;
				}	//clip filter(black) ZERO_FILL
				else {
				Color pixel = new Color(in.getRGB(x-1, y-1));	// get the color
				rArrayred[y*widthp+x] = pixel.getRed();			// red component
				rArraygreen[y*widthp+x] = pixel.getGreen();		// green component
				rArrayblue[y*widthp+x] = pixel.getBlue();		// blue component
				}
			}
		height=heightp;	
		width=widthp;
		for (int y = 1; y < height-1; y++)
			for (int x = 1; x < width-1; x++) {
				int [] rNeighbourred = {
					rArrayred[(y-1)*width+x-1], rArrayred[(y-1)*width+x], rArrayred[(y-1)*width+x+1],
					rArrayred[y*width+x-1], rArrayred[y*width+x], rArrayred[y*width+x+1],
					rArrayred[(y+1)*width+x-1], rArrayred[(y+1)*width+x], rArrayred[(y+1)*width+x+1]						
				};	//neighbour pixels in red
				int [] rNeighbourgreen = {
					rArraygreen[(y-1)*width+x-1], rArraygreen[(y-1)*width+x], rArraygreen[(y-1)*width+x+1],
					rArraygreen[y*width+x-1], rArraygreen[y*width+x], rArraygreen[y*width+x+1],
					rArraygreen[(y+1)*width+x-1], rArraygreen[(y+1)*width+x], rArraygreen[(y+1)*width+x+1]						
					};	//neighbour pixels in red
				int [] rNeighbourblue = {
					rArrayblue[(y-1)*width+x-1], rArrayblue[(y-1)*width+x], rArrayblue[(y-1)*width+x+1],
					rArrayblue[y*width+x-1], rArrayblue[y*width+x], rArrayblue[y*width+x+1],
					rArrayblue[(y+1)*width+x-1], rArrayblue[(y+1)*width+x], rArrayblue[(y+1)*width+x+1]						
					};	//neighbour pixels in blue
				
				/*median filter*/
				float red = 0;
				Arrays.sort(rNeighbourred);	//sort red neighbours
				red=rNeighbourred[4];		//get middle element
				red = Math.max(0, red);
				red = Math.min(255, red);
				
				float green = 0;
				Arrays.sort(rNeighbourgreen);	//sort green neighbours
				green=rNeighbourgreen[4];		//get middle element
				green = Math.max(0, green);
				green = Math.min(255, green);
				
				float blue = 0;
				Arrays.sort(rNeighbourblue);		//sort blue neighbours
				blue=rNeighbourblue[4];			//get middle element
				blue = Math.max(0, blue);
				blue = Math.min(255, blue);
				out.setRGB(x-1, y-1, (new Color( (int)red, (int)green, (int)blue)).getRGB());	//output color-ed pixel
			}

		repaint();
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.exit(0);

		if (ke.getKeyChar() == 'i' || ke.getKeyChar() == 'I') { // Input a new file
			loadImage();
			repaint();
		} else if (ke.getKeyChar() == 's' || ke.getKeyChar() == 'S') {// Save the processed image
			saveImage();
		} else if (ke.getKeyChar() == 'p' || ke.getKeyChar() == 'P') {// Image Processing
			processing();
		}
	}

	private void loadImage() {
		JFileChooser chooser = new JFileChooser("."); // initial current directory
		chooser.setFileFilter(new ImageFileFilter());
		chooser.setAccessory(new ImagePreview(chooser));
		int rval = chooser.showOpenDialog(null);
		if (rval == JFileChooser.APPROVE_OPTION) {
			inputFile = chooser.getSelectedFile();
			try {
				in = ImageIO.read(inputFile);
				width = in.getWidth();
				height = in.getHeight();
				if (in.getType() != BufferedImage.TYPE_INT_RGB) {
					BufferedImage bi2 = new BufferedImage(width, height,
							BufferedImage.TYPE_INT_RGB);
					Graphics big = bi2.getGraphics();
					big.drawImage(in, 0, 0, null);
					in = bi2;
				}
				out = in;
			} catch (IOException e) {
				System.out.println("Image could not be read");
				System.exit(1);
			}
		}
	}

	private void saveImage() {
		String fileName = inputFile.getName();

		File saveFile = new File("P" + fileName);
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new ImageFileFilter());
		chooser.setAccessory(new ImagePreview(chooser));
		chooser.setSelectedFile(saveFile);
		chooser.setCurrentDirectory(inputFile);
		int rval = chooser.showSaveDialog(null);
		if (rval == JFileChooser.APPROVE_OPTION) {
			saveFile = chooser.getSelectedFile();
			fileName = saveFile.getName();
			int pos = fileName.lastIndexOf('.');
			String ext = fileName.substring(pos + 1);

			/*
			 * Write the processed image in the selected format, to the file
			 * chosen by the user.
			 */
			try {
				ImageIO.write(out, ext, saveFile);
			} catch (IOException ex) {
			}
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	// ////////////////////////////////////////////////////////////////////////////////
	// Classes from Oracle tutorial for JFileChooser

	/* Modified from ImagePreview.java by FileChooserDemo2.java. */
	class ImageFileFilter extends FileFilter {

		private String description;
		private String[] extensions = getFormats(false);
		int count = extensions.length;

		// Accept all image files that java can deal with
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			}
			String path = file.getAbsolutePath();
			for (int i = 0; i < count; i++) {
				String ext = extensions[i];
				int pointPos = path.length() - ext.length() - 1;
				if (pointPos >= 0 && path.endsWith(ext)
						&& (path.charAt(pointPos) == '.')) {
					return true;
				}
			}
			return false;
		}

		// The description of this filter
		public String getDescription() {

			description = "Image Files (";
			for (int i = 0; i < count - 1; i++) {
				description += "." + extensions[i] + ", ";
			}
			description += "." + extensions[count - 1] + ")";

			return (description);
		}

		// Return the formats sorted alphabetically and in lower case
		public String[] getFormats(boolean io) {
			String[] formats;
			formats = ImageIO.getWriterFormatNames();

			TreeSet<String> formatSet = new TreeSet<String>();
			for (String s : formats) {
				formatSet.add(s.toLowerCase());
			}
			return formatSet.toArray(new String[0]);
		}
	}

	/* ImagePreview.java by FileChooserDemo2.java. */
	class ImagePreview extends JComponent implements PropertyChangeListener {
		ImageIcon thumbnail = null;
		File file = null;

		public ImagePreview(JFileChooser fc) {
			setPreferredSize(new Dimension(100, 50));
			fc.addPropertyChangeListener(this);
		}

		public void loadImage() {
			if (file == null) {
				thumbnail = null;
				return;
			}

			// Don't use createImageIcon (which is a wrapper for getResource)
			// because the image we're trying to load is probably not one
			// of this program's own resources.
			ImageIcon tmpIcon = new ImageIcon(file.getPath());
			if (tmpIcon != null) {
				if (tmpIcon.getIconWidth() > 90) {
					thumbnail = new ImageIcon(tmpIcon.getImage()
							.getScaledInstance(90, -1, Image.SCALE_DEFAULT));
				} else { // no need to miniaturize
					thumbnail = tmpIcon;
				}
			}
		}

		public void propertyChange(PropertyChangeEvent e) {
			boolean update = false;
			String prop = e.getPropertyName();

			// If the directory changed, don't show an image.
			if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
				file = null;
				update = true;

				// If a file became selected, find out which one.
			} else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
				file = (File) e.getNewValue();
				update = true;
			}

			// Update the preview accordingly.
			if (update) {
				thumbnail = null;
				if (isShowing()) {
					loadImage();
					repaint();
				}
			}
		}

		protected void paintComponent(Graphics g) {
			if (thumbnail == null) {
				loadImage();
			}
			if (thumbnail != null) {
				int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
				int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;

				if (y < 0) {
					y = 0;
				}

				if (x < 5) {
					x = 5;
				}
				thumbnail.paintIcon(this, g, x, y);
			}
		}
	}

}
