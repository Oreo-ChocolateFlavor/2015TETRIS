import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;

import javax.swing.*;

public class Tetris_display extends JComponent {
    //Tetris객체를 모든 부분에 넣을 경우 갱신 및 처리가 어려울 것으로 판단되어, 실제 게임을 돌릴 수 있는 기능은 없고
    //정보를 받아 다른 플레이어의 상태를 표시 할 수 있는 객체를 새로 만들었다.
    private final static byte ROWS = 18;
    private final static byte COLUMNS = 10;
    private int grid[][] = new int[ROWS][COLUMNS]; //전체 게임보드 판이 될 부분이다.
    private int num_rows_deleted = 0;
    private final static int EMPTY = -1;
    private final static int DELETED_ROWS_PER_LEVEL = 5;
    private GridCanvas game_grid = new GridCanvas(grid, true);
    private final static Color PIECE_COLORS[] = { //블럭들의 색을 지정하는 부분이다.
            new Color(0xFF00FF), // fucia
            new Color(0xDC143C), // crimson
            new Color(0x00CED1), // dark turquoise
            new Color(0xFFD700), // gold
            new Color(0x32CD32), // lime green
            new Color(0x008080), // teal
            new Color(0xFFA500), // orange
    };
    private final static Color BACKGROUND_COLORS[] = { //배경색을 지정하는 부분이다.
            new Color(0xFFDAB9), // peachpuff
            new Color(0xFFC0CB), // pink
            new Color(0xFF99CC), // hot pink
            new Color(0x0099CC), // sky blue
            new Color(0x9966CC), // lavender
    };
    private final static Color BACKGROUND_COLOR = new Color(0x99FFCC);

    private class GridCanvas extends DoubleBufferedCanvas { //실제 인자들을 가지고 Grid에 그려주는 클래스이다.
        private int grid[][];
        private boolean paint_background;
        public GridCanvas(int[][] grid, boolean do_background) {
            this.grid = grid;
            paint_background = do_background;
            clear();
        }

        private void clear() {
            for(int i=0; i<grid.length; i++)
                for(int j=0; j<grid[0].length; j++)
                    grid[i][j] = EMPTY;
        }
        public Dimension getPreferredSize() {
            return new Dimension(grid[0].length * 30, grid.length * 30);
        }
        public void paint(Graphics g) {
            g = this.startPaint(g); // returned g paints into offscreen image
            int width = this.getSize().width;
            int height = this.getSize().height;
            g.clearRect(0, 0, width, height);
            int cell_size, xstart, ystart;
            double panel_aspect_ratio = (double)width/height;
            double grid_aspect_ratio = (double)grid[0].length/grid.length;
            if(panel_aspect_ratio > grid_aspect_ratio) {
                // extra space on sides
                cell_size = (int)((double)height/grid.length + 0.5);
                xstart = (int)(width/2 - (grid[0].length/2.0 * cell_size + 0.5));
                ystart = 0;
            }
            else {
                // extra vertical space
                cell_size = (int)((double)width/grid[0].length + 0.5);
                xstart = 0;
                ystart = (int)(height/2 - (grid.length/2.0 * cell_size + 0.5));
            }
            if(paint_background) {
                g.setColor(BACKGROUND_COLORS[(num_rows_deleted / DELETED_ROWS_PER_LEVEL) % BACKGROUND_COLORS.length]);
                g.fillRect(xstart, ystart, COLUMNS*cell_size, ROWS*cell_size);
            }
            for(int i=0; i<grid.length; i++) {
                for(int j=0; j<grid[0].length; j++) {
                    if(grid[i][j] != EMPTY) {
                        g.setColor(PIECE_COLORS[grid[i][j]]);
                        int x = xstart + j*cell_size;
                        int y = ystart + i*cell_size;
                        g.fill3DRect(x, y, cell_size, cell_size, true);
                    }
                }
            }
            this.endPaint(); // paints accumulated image in one shot
        }
    } // end class GridCanvas

    public void setGrid(int tmpgrid[][]) //grid를 내가 원하는 배열로 설정하는 부분이다. 실제 이 함수를 이용해서 들어오는 정보를 지속적으로 갱신한다.
    {
        for(int i=0; i<18; i++)
        {
            for(int j=0; j<10; j++)
            {
                this.grid[i][j] = tmpgrid[i][j];
            }
        }
    }

    public void init() { //그리기를 시작 할 때 초기화 하는 부분이다.

        Panel tmp = new Panel(new BorderLayout());
        tmp.setBackground(BACKGROUND_COLOR);

        Panel stats_panel = new Panel(new GridLayout(4, 2));
        tmp = new Panel(new BorderLayout());
        tmp.setBackground(BACKGROUND_COLOR);
        tmp.add("Center", stats_panel);

        // finaly, add all the main panels to the applet panel
        this.setLayout(new GridLayout(1, 2));
        this.add(game_grid);
        this.setBackground(BACKGROUND_COLOR);
        this.validate();
    }
}
