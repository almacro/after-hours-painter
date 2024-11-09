package org.example;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JColorChooser;

/**
 *
 * @author almacro
 */
public class Painter extends Frame 
        implements ActionListener, ItemListener, MouseListener, MouseMotionListener {
    
    MenuBar menubar;
    Menu menuFile, menuDraw, menuEffects;
    MenuItem menuitemNew, menuitemOpen, menuitemSave, menuitemExit;
    CheckboxMenuItem menuitemDrawLine, menuitemDrawEllips, menuitemDrawRect,
            menuitemDrawRoundRect, menuitemDrawFree, menuitemDrawText;
    CheckboxMenuItem menuitemPlain, menuitemSolid, menuitemGrad, 
            menuitemTexture, menuitemTransparent, menuitemDrawThickLine, 
            menuitemShadow;
    MenuItem menuitemColor;
    
    FileDialog dialog;
    OkCancelDialog textDialog;
    
    BufferedImage bufferedImage, tileImage;
    Image image;
    Color color = Color.BLACK;
    Paint paint;
    Composite composite;
    
    int imageWidth = 300;
    int imageHeight = 300;
    int offsetX = 50, offsetY = 50;
    
    Point start = new Point();
    Point end = new Point();
    
    boolean rounded, line, ellipse, rectangle, draw, text;    
    boolean solid, shade, texture, transparent, thick, shadow;
    
    String drawText;
    
    boolean mouseUp, adjusted, dragging;
    int dots = 0;
    java.util.List<Point> dot = new ArrayList<>();
    
    public Painter() {
        setLayout(null);
        
        addMouseListener(this);
        addMouseMotionListener(this);
    
        menubar = new MenuBar();
        
        menuFile = new Menu("File");
        menuDraw = new Menu("Draw");
        menuEffects = new Menu("Effects");
        
        menuitemNew = new MenuItem("New");
        menuFile.add(menuitemNew);
        menuitemNew.addActionListener(this);
        
        menuitemOpen = new MenuItem("Open...");
        menuFile.add(menuitemOpen);
        menuitemOpen.addActionListener(this);
        
        menuitemSave = new MenuItem("Save As...");
        menuFile.add(menuitemSave);
        menuitemSave.addActionListener(this);
        
        menuitemExit = new MenuItem("Exit");
        menuFile.add(menuitemExit);
        menuitemExit.addActionListener(this);
        
        menuitemDrawLine = new CheckboxMenuItem("Draw lines");
        menuDraw.add(menuitemDrawLine);
        menuitemDrawLine.addItemListener(this);
        
        menuitemDrawEllips = new CheckboxMenuItem("Draw ellipses");
        menuDraw.add(menuitemDrawEllips);
        menuitemDrawEllips.addItemListener(this);
        
        menuitemDrawRect = new CheckboxMenuItem("Draw rectangles");        
        menuDraw.add(menuitemDrawRect);
        menuitemDrawRect.addItemListener(this);
        
        menuitemDrawRoundRect = new CheckboxMenuItem("Draw rounded rectangles");        
        menuDraw.add(menuitemDrawRoundRect);
        menuitemDrawRoundRect.addItemListener(this);
        
        menuitemDrawFree = new CheckboxMenuItem("Draw freehand");        
        menuDraw.add(menuitemDrawFree);
        menuitemDrawFree.addItemListener(this);
        
        menuitemDrawText = new CheckboxMenuItem("Draw text");  
        menuDraw.add(menuitemDrawText);
        menuitemDrawText.addItemListener(this);
        
        menuitemPlain = new CheckboxMenuItem("Plain");
        menuEffects.add(menuitemPlain);
        menuitemPlain.addItemListener(this);
        
        menuitemSolid = new CheckboxMenuItem("Solid fill");
        menuEffects.add(menuitemSolid);
        menuitemSolid.addItemListener(this);
        
        menuitemGrad = new CheckboxMenuItem("Gradient fill");
        menuEffects.add(menuitemGrad);
        menuitemGrad.addItemListener(this);
        
        menuitemTexture = new CheckboxMenuItem("Texture fill");
        menuEffects.add(menuitemTexture);
        menuitemTexture.addItemListener(this);
        
        menuitemTransparent = new CheckboxMenuItem("Transparent");
        menuEffects.add(menuitemTransparent);
        menuitemTransparent.addItemListener(this);
        
        menuitemDrawThickLine = new CheckboxMenuItem("Draw thick lines");
        menuEffects.add(menuitemDrawThickLine);
        menuitemDrawThickLine.addItemListener(this);
        
        menuitemShadow = new CheckboxMenuItem("Drop shadow");
        menuEffects.add(menuitemShadow);
        menuitemShadow.addItemListener(this);
        
        menuitemColor = new MenuItem("Select color...");
        menuEffects.add(menuitemColor);
        menuitemColor.addActionListener(this);
        
        menubar.add(menuFile);
        menubar.add(menuDraw);
        menubar.add(menuEffects);
        
        setMenuBar(menubar);
        
        dialog = new FileDialog(this, "File Dialog");
        bufferedImage = new BufferedImage(imageWidth, imageHeight, 
                BufferedImage.TYPE_INT_BGR);
        setSize(400, 400);
        
        setTitle("Painter");
        setVisible(true);
        
        image = createImage(imageWidth, imageHeight);
        textDialog = new OkCancelDialog(this, "Enter your text", true);
        
        try {
            URL u = getClass().getClassLoader().getResource("tile.jpg");
            tileImage = ImageIO.read(u);
        } catch(java.io.IOException ioe) {
            System.err.println("Need tile.jpg");
            System.exit(0);
        }
        
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    
    public void paint(Graphics g) {
        Graphics2D gImage;

        if(!dragging && !adjusted) {
            if(image == null) {
                image = createImage(imageWidth, imageHeight);
            }
            gImage = (Graphics2D) image.getGraphics();
        } else {
            gImage = (Graphics2D) g;
            g.drawImage(image, offsetX, offsetY, this);
            g.drawRect(offsetX, offsetY, imageWidth, imageHeight);
            gImage.clip(new Rectangle2D.Double(offsetX, offsetY, imageWidth, imageHeight));
        }
        composite = gImage.getComposite();
        
        if(color != null) {
            gImage.setColor(color);
        } else {
            gImage.setColor(new Color(0,0,0));
        }
        
        if(thick) {
            gImage.setStroke(new BasicStroke(10));
        } else {
            gImage.setStroke(new BasicStroke(1));
        }
        
        if(mouseUp || dragging) {
            Point tempStart, tempEnd;
            
            tempStart = new Point(
                    Math.min(end.x, start.x), 
                    Math.min(end.y, start.y));
            tempEnd = new Point(
                    Math.max(end.x, start.x), 
                    Math.max(end.y, start.y));
            
            tempStart = new Point(
                    Math.max(tempStart.x, offsetX), 
                    Math.max(tempStart.y, offsetY));
            tempEnd = new Point(
                    Math.max(tempEnd.x, offsetX), 
                    Math.max(tempEnd.y, offsetY));
            
            tempStart = new Point(
                    Math.min(tempStart.x, bufferedImage.getWidth() + offsetX),
                    Math.min(tempStart.y, bufferedImage.getHeight() + offsetY));
            tempEnd = new Point(
                    Math.min(tempEnd.x, bufferedImage.getWidth() + offsetX),
                    Math.min(tempEnd.y, bufferedImage.getHeight() + offsetY));
            
            if(!adjusted) {
                tempEnd.x -= offsetX;
                tempEnd.y -= offsetY;
                tempStart.x -= offsetX;
                tempStart.y -= offsetY;
                end.x -= offsetX;
                end.y -= offsetY;
                start.x -= offsetX;
                start.y -= offsetY;
                adjusted = true;
            }
            int width = tempEnd.x - tempStart.x;
            int height = tempEnd.y - tempStart.y;
                        
            // Drawing Lines
            if(line) {
                Line2D.Double drawLine = new Line2D.Double(
                        start.x, start.y,
                        end.x, end.y
                );
                
                if(shadow) {
                    paint = gImage.getPaint();
                    composite = gImage.getComposite();
                    
                    gImage.setPaint(Color.BLACK);
                    gImage.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, 0.3f));
                    
                    Line2D.Double line2 = new Line2D.Double(
                            start.x + 9, start.y + 9,
                            end.x, end.y);
                    gImage.draw(line2);
                    gImage.setPaint(paint);
                    gImage.setComposite(composite);
                }
                
                gImage.draw(drawLine);
            }
            
            // Drawing Ellipses
            if(ellipse && width != 0 && height != 0) {
                Ellipse2D.Double ellipse = new Ellipse2D.Double(
                        tempStart.x, tempStart.y,
                        width, height);
                if(shadow) {
                    paint = gImage.getPaint();
                    composite = gImage.getComposite();
                    gImage.setPaint(Color.BLACK);
                    gImage.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, 0.3f));
                    Ellipse2D.Double ellipse2 = new Ellipse2D.Double(
                        tempStart.x + 9, tempStart.y + 9,
                        width, height);
                    if(solid || shade || transparent || texture) {
                        gImage.fill(ellipse2);
                    } else {
                        gImage.draw(ellipse2);
                    }
                    gImage.setPaint(paint);
                    gImage.setComposite(composite);
                }
                
                if(solid) {
                    gImage.setPaint(color);
                }
                
                if(shade) {
                    gImage.setPaint(new GradientPaint(
                            tempStart.x, tempStart.y, color,
                            tempEnd.x, tempEnd.y, Color.BLACK));
                }
                
                if(texture) {
                    Rectangle2D.Double anchor = new Rectangle2D.Double(
                        0, 0, tileImage.getWidth(), tileImage.getHeight());
                    
                    TexturePaint texturePaint = new TexturePaint(tileImage, anchor);
                    
                    gImage.setPaint(texturePaint);
                }
                
                if(transparent) {
                    gImage.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.1f));
                }
                
                if(solid || shade || transparent || texture) {
                    gImage.fill(ellipse);
                } else {
                    gImage.draw(ellipse);
                }
                
                if(transparent) {
                    gImage.setComposite(composite);
                }
            }
            
            // Drawing Rectangles
            if(rectangle && width != 0 && height != 0) {
                System.out.println("rect");
                Rectangle2D.Double rectangle = new Rectangle2D.Double(
                        tempStart.x, tempStart.y,
                        width, height);
                
                if(shadow) {
                    paint = gImage.getPaint();
                    composite = gImage.getComposite();
                    
                    gImage.setPaint(Color.BLACK);
                    gImage.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.3f));
                    
                    Rectangle2D.Double rectangle2 = new Rectangle2D.Double(
                            tempStart.x + 9, tempStart.y + 9,
                            width, height);
                    
                    if(solid || shade || transparent || texture) {
                        gImage.fill(rectangle2);
                    } else {
                        gImage.draw(rectangle2);
                    }
                    
                    gImage.setPaint(paint);
                    gImage.setComposite(composite);
                }
                
                if(solid) {
                    gImage.setPaint(color);
                }
                
                if(shade) {
                    gImage.setPaint(new GradientPaint(
                            tempStart.x, tempStart.y, color,
                            tempEnd.x, tempEnd.y, Color.BLACK));
                }
                
                if(transparent) {
                    gImage.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.3f));
                }
                
                if(texture) {
                    Rectangle2D.Double anchor = new Rectangle2D.Double(
                            0, 0,
                            tileImage.getWidth(), tileImage.getHeight());
                    TexturePaint texturePaint =
                            new TexturePaint(tileImage, anchor);
                    
                    gImage.setPaint(texturePaint);
                }
                
                if(solid || shade || texture || transparent) {
                    gImage.fill(rectangle);
                } else {
                    gImage.draw(rectangle);
                }
                
                if(transparent) {
                    gImage.setComposite(composite);
                }
            }
            
            // Drawing Rounded Rectangles
            if(rounded && width != 0 && height != 0) {
                RoundRectangle2D.Double round = new RoundRectangle2D.Double(
                        tempStart.x, tempStart.y,
                        width, height,
                        10, 10);
                
                if(shadow) {
                    paint = gImage.getPaint();
                    composite = gImage.getComposite();
                    
                    gImage.setPaint(Color.BLACK);
                    gImage.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.3f));
                    
                    RoundRectangle2D.Double round2 = new RoundRectangle2D.Double(
                            tempStart.x + 9, tempStart.y + 9,
                            width, height,
                            10, 10);
                    
                    if(solid || shade || transparent || texture) {
                        gImage.fill(round2);
                    } else {
                        gImage.draw(round2);
                    }
                    
                    gImage.setPaint(paint);
                    gImage.setComposite(composite);
                }
                
                if(solid) {
                    gImage.setPaint(color);
                }
                
                if(shade) {
                    gImage.setPaint(new GradientPaint(
                            tempStart.x, tempStart.y, color,
                            tempEnd.x, tempEnd.y, Color.BLACK));
                }
                
                if(transparent) {
                    gImage.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.3f));
                }
                
                if(texture) {
                    Rectangle2D.Double anchor = new Rectangle2D.Double(
                        0, 0,
                        tileImage.getWidth(), tileImage.getHeight());
                    
                    TexturePaint texturePaint = new TexturePaint(
                            tileImage, anchor);
                    
                    gImage.setPaint(texturePaint);
                }
                
                if(solid || shade || texture || transparent) {
                    gImage.fill(round);
                } else {
                    gImage.draw(round);
                }
                
                if(transparent) {
                    gImage.setComposite(composite);
                }
            }
            
            if(draw) {
                Line2D.Double drawLine;
                
                if(shadow) {
                    paint = gImage.getPaint();
                    composite = gImage.getComposite();
                    
                    gImage.setPaint(Color.BLACK);
                    gImage.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.3f));
                    
                    for(int i = 0; i < dot.size() - 1; i++) {
                        Point p1 = dot.get(i);
                        Point p2 = dot.get(i+1);
                        if(dragging) {
                            drawLine = new Line2D.Double(
                                    p1.x + 9, p1.y + 9,
                                    p2.x + 9, p2.y + 9);
                        } else {
                            drawLine = new Line2D.Double(
                                    p1.x - offsetX + 9, p1.y - offsetY + 9,
                                    p2.x - offsetX + 9, p2.y - offsetY + 9);
                        }
                        gImage.draw(drawLine);
                    }
                    
                    gImage.setPaint(paint);
                    gImage.setComposite(composite);
                }
                
                for(int i = 0; i < dot.size() - 1; i++) {
                        Point p1 = dot.get(i);
                        Point p2 = dot.get(i + 1);

                    if (dragging) {
                        drawLine = new Line2D.Double(
                                p1.x, p1.y,
                                p2.x, p2.y);
                    } else {
                        drawLine = new Line2D.Double(
                                p1.x - offsetX, p1.y - offsetY,
                                p2.x - offsetX, p2.y - offsetY);
                    }
                    gImage.draw(drawLine);
                }
                if(!dragging) {
                    dot.clear();
                }
            }
        }
        
        if(text) {
            if(drawText != null && end != null && end.x != 0 && end.y != 0) {
                if(shadow) {
                    paint = gImage.getPaint();
                    composite = gImage.getComposite();
                    
                    gImage.setPaint(Color.BLACK);
                    gImage.setComposite(AlphaComposite.getInstance( 
                        AlphaComposite.SRC_OVER, 0.3f));
                    
                    gImage.drawString(drawText, end.x + 9, end.y + 9);
                    
                    gImage.setPaint(paint);
                    gImage.setComposite(composite);
                }
                
                gImage.drawString(drawText, end.x, end.y);
            }
        }
        
        if(!dragging) {
            g.drawImage(image, offsetX, offsetY, this);
        }
        g.setColor(Color.BLACK);
        gImage.setStroke(new BasicStroke(1));
        g.drawRect(offsetX, offsetY, imageWidth, imageHeight);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == menuitemOpen) {
            try {
                dialog.setMode(FileDialog.LOAD);
                dialog.setVisible(true);
                String infile = dialog.getFile();
                if (!infile.equals("")) {
                    File inputFile = new File(dialog.getDirectory() + infile);
                    bufferedImage = ImageIO.read(inputFile);
                    if (bufferedImage != null) {
                        image = createImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                        Graphics2D g2d = (Graphics2D) image.getGraphics();
                        g2d.drawImage(bufferedImage, null, 0, 0);
                        imageWidth = bufferedImage.getWidth();
                        imageHeight = bufferedImage.getHeight();
                        setSize(imageWidth + 100, imageHeight + 90);
                        repaint();
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        
        if(e.getSource() == menuitemSave) {
            saveImage();
        }
        
        if(e.getSource() == menuitemNew) {
            bufferedImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_BGR);
            image = createImage(300,300);
            resetStartEnd();
            repaint();
        }
        
        if(e.getSource() == menuitemDrawText) {
            textDialog.setVisible(true);
            drawText = textDialog.data;
            setFlagsFalse();
            text = true;
            menuitemDrawText.setState(true);
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemColor) {
            color = JColorChooser.showDialog(this, "Select your color", Color.BLACK);
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemExit) {
            // TODO should check if image needs to be saved
            saveImage();
            System.exit(0);
        }
    }

    void saveImage() {
        dialog.setMode(FileDialog.SAVE);
        dialog.setVisible(true);
        try {
            String outfile = dialog.getFile();
            if (!outfile.equals("")) {
                if(outfile.lastIndexOf(".") == -1) {
                    outfile = outfile + ".png";
                }
                File outputFile = new File(dialog.getDirectory() + outfile);
                bufferedImage.createGraphics().drawImage(image, 0, 0, this);
                ImageIO.write(bufferedImage,
                        outfile.substring(outfile.length() - 3, outfile.length()),
                        outputFile);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == menuitemDrawLine) {
            setFlagsFalse();
            line = true;
            menuitemDrawLine.setState(true);
            resetStartEnd();
        }
        
        if (e.getSource() == menuitemDrawEllips) {
            setFlagsFalse();
            ellipse = true;
            menuitemDrawEllips.setState(true);
            resetStartEnd();
        }
        
        if (e.getSource() == menuitemDrawRect) {
            setFlagsFalse();
            rectangle = true;
            menuitemDrawRect.setState(true);
            resetStartEnd();
        }
        
        if (e.getSource() == menuitemDrawRoundRect) {
            setFlagsFalse();
            rounded = true;
            menuitemDrawRoundRect.setState(true);
            resetStartEnd();
        }
        
        if (e.getSource() == menuitemDrawFree) {
            setFlagsFalse();
            draw = true;
            menuitemDrawFree.setState(true);
            resetStartEnd();
        }
        
        if (e.getSource() == menuitemDrawText) {
            setFlagsFalse();
            text = true;
            menuitemDrawText.setState(true);
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemSolid) {
            solid = !solid;
            if(solid) {
                texture = false;
                shade = false;
            }
            menuitemSolid.setState(solid);
            menuitemGrad.setState(shade);
            menuitemTexture.setState(texture);
            menuitemPlain.setState(false);
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemGrad) {
            shade = !shade;
            if(shade) {
                solid = false;
                texture = false;
            }
            menuitemSolid.setState(solid);
            menuitemGrad.setState(shade);
            menuitemTexture.setState(texture);
            menuitemPlain.setState(false);
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemTexture) {
            texture = !texture;
            if(texture) {
                shade = false;
                solid = false;
            }
            menuitemSolid.setState(solid);
            menuitemGrad.setState(shade);
            menuitemTexture.setState(texture);
            menuitemPlain.setState(false);
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemTransparent) {
            transparent = !transparent;
            menuitemTransparent.setState(transparent);
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemDrawText) {
            textDialog.setVisible(true);
            drawText = textDialog.data;
            setFlagsFalse();
            text = true;
            menuitemDrawText.setState(true);
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemDrawThickLine) {
            thick = menuitemDrawThickLine.getState();
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemPlain) {
            menuitemSolid.setState(false);
            menuitemGrad.setState(false);
            menuitemTexture.setState(false);
            menuitemTransparent.setState(false);
            menuitemPlain.setState(true);
            shade = false;
            solid = false;
            transparent = false;
            texture = false;
            resetStartEnd();
        }
        
        if(e.getSource() == menuitemShadow) {
            shadow = menuitemShadow.getState();
            resetStartEnd();
        }
    }
    
    void setFlagsFalse() {
        rounded = false;
        line = false;
        ellipse = false;
        rectangle = false;
        draw = false;
        text = false;
        
        menuitemDrawLine.setState(false);
        menuitemDrawEllips.setState(false);
        menuitemDrawRect.setState(false);
        menuitemDrawRoundRect.setState(false);
        menuitemDrawFree.setState(false);
        menuitemDrawText.setState(false);
    }
    
    void resetStartEnd() {
        start.x = -20;
        start.y = -20;
        end.x = -20;
        end.y = -20;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        mouseUp = false;
        start = new Point(e.getX(), e.getY());
        adjusted = true;
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(rounded || line || ellipse || rectangle || draw || text) {
            end = new Point(e.getX(), e.getY());
            mouseUp = true;
            dragging = false;
            adjusted = false;
            repaint();
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        dragging = true;
        Rectangle rect = new Rectangle(offsetX, offsetY, imageWidth, imageHeight); 
        if(rect.contains(e.getX(), e.getY())) {
            if(draw) {
                dot.add(new Point(e.getX(), e.getY()));
            }
        }
        if(rect.contains(start.x, start.y)) {
            end = new Point(e.getX(), e.getY());
            repaint();
        }
        
    }

    @Override  public void mouseClicked(MouseEvent e) {}
    @Override  public void mouseEntered(MouseEvent e) {}
    @Override  public void mouseExited (MouseEvent e) {}
    @Override  public void mouseMoved  (MouseEvent e) {}
    
    public static void main(String[] args) {
        new Painter();
    }
}
