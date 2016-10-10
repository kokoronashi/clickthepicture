// First scoll , then cut

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;

public class SI extends JFrame {
    private JButton jbtStart = new JButton("Start"); //
    private JButton jbtEnlarge = new JButton("Enlarge"); // 
    private JButton jbtShrink = new JButton("Shrink");
    private CirclePanel canvas = new CirclePanel();
    private int photo_id = 0;
    private int photo_nums = 0;
    private BufferedImage [] image;
    public SI() {
        JPanel panel = new JPanel();
        panel.add(jbtStart);
        panel.add(jbtEnlarge);
        panel.add(jbtShrink);
        
        File file = null;
        String [] paths;
        try {
            file = new File("image");
            paths = file.list();
            photo_nums = paths.length;
            image = new BufferedImage[photo_nums];
            for(int i = 0 ; i < photo_nums ; i ++) {
                image[i]  = Read_Image("image/"+paths[i]);
                // image[i] = ImageIO.read(new File("image/"+paths[i])); 
            }
            canvas.setFore(image[0]);
            canvas.setBack(image[1]);
            photo_id = photo_id ++;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        this.add(canvas,BorderLayout.CENTER);
        this.add(panel,BorderLayout.SOUTH);   
        
        jbtStart.addActionListener(new StartListener()); // Start Listener
        jbtEnlarge.addActionListener(new EnlargeListener()); // Enlarge Listener
        jbtShrink.addActionListener(new ShrinkListener()); // Shrink Listener
    }

    public static void main(String args[]) {
        SI frame = new SI();
        frame.setSize(416,476);
        frame.setTitle("Hello ACG");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public BufferedImage Read_Image(String photo_path) {
        BufferedImage bi = null;
        // import image
        try {
            bi = ImageIO.read(new File(photo_path));
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        //scoll
        int width = bi.getWidth();
        int height = bi.getHeight();

        int toWidth = 0;
        int toHeight = 0;

        int x_point = 0;
        int y_point = 0;

        if(width <= height) { //Litter edge go to 400 and cut the big edge
            toWidth = 400;
            toHeight = (int) ((toWidth * 1.0f / width) * height);
        
            y_point = (toHeight - 400) / 2;
        }
        else {
            toHeight = 400;
            toWidth = (int) ((toHeight * 1.0f / height) * width);
            
            x_point = (toWidth - 400) / 2;
        }

        BufferedImage mid = new BufferedImage(toWidth, toHeight,  
                    BufferedImage.TYPE_INT_RGB);  
  
        mid.getGraphics().drawImage(bi.getScaledInstance(toWidth, toHeight,  
                            java.awt.Image.SCALE_SMOOTH), 0, 0, null); 
        BufferedImage mid2 = mid.getSubimage(x_point,y_point,400 ,400 );
        BufferedImage result = new BufferedImage(400,400,BufferedImage.TYPE_INT_RGB);
        
        //System.out.printf("minx : %d miny : %d\n",result.getMinX(),result.getMinY());
        result.getGraphics().drawImage(mid2,0,0,null);
        System.out.printf("%d %d\n",x_point,y_point);
        return result;
    }

    public class StartListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(canvas.getStartStatus() == false) { 
                if(photo_id == photo_nums - 1) {
                    canvas.setFore(image[photo_id]);
                    canvas.setBack(image[0]);
                    photo_id = 0;
                }
                else {
                    canvas.setFore(image[photo_id]);
                    canvas.setBack(image[photo_id+1]);
                    photo_id ++;
                }
                canvas.Start();
            }
        }
    }

    public class EnlargeListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.Enlarge();
        }
    }

    public class ShrinkListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.Shrink();
        }
    }
}

class CirclePanel extends JPanel {
    private boolean is_Start = false;
    private int radius = 50; // Default
    private BufferedImage fore;
    private BufferedImage back;
    private int imageHeight = 400;
    private int imageWidth = 400;

    public void Start() {
        if(is_Start == false) {
            is_Start = true;
            radius = 50;
            repaint();
        }
    }

    public boolean getStartStatus() {
        return is_Start;
    }

    public void setFore(BufferedImage _image) {
        fore = _image;
    }

    public void setBack(BufferedImage _image) {
        back = _image;
    }

    public void Enlarge() {
        if(radius == imageWidth / 2 || radius == imageHeight / 2) return ;
        radius += 5;
        repaint();
    }

    public void Shrink() {
        radius -= 5;
        if(radius == 0) return ;
        if(radius < 0) radius = 0;
        repaint();
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(is_Start == false) {
            g.drawImage(fore,0,0,getWidth(),getHeight(),this);
        }
        else {
            int centerX = imageWidth / 2;
            int centerY = imageHeight / 2;

            if(radius == centerX || radius == centerY || radius == 0 || radius == 0) {
                g.drawImage(back,0,0,getWidth(),getHeight(),this);
                is_Start = false;
                return ;
            }

            BufferedImage now = deepCopy(back);
            for (int i = 0 ; i < imageWidth ; i ++) {
                for (int j = 0 ; j < imageHeight ; j ++) {
                    if((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY) >= radius * radius) {
                        Object data = fore.getRaster().getDataElements(i, j, null);
                        int red  = fore.getColorModel().getRed(data);
                        int green = fore.getColorModel().getGreen(data);
                        int blue = fore.getColorModel().getBlue(data);
                        int rgb = (red * 256 + green) * 256 + blue;
                        now.setRGB(i,j,rgb);
                    }
                }
            }
            g.drawImage(now,0,0,getWidth(),getHeight(),this);
        }
        // g.drawImage(back,0,0,getWidth(),getHeight(),this);
        // g.drawOval(getWidth() / 2 - radius, getHeight() / 2 - radius , 2 * radius , 2 * radius);
    }
}
