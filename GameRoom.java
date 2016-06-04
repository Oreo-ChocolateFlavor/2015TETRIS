import javax.swing.*;

/**
 * Created by JaeSeung on 2016. 6. 3..
 */
public class GameRoom extends javax.swing.JFrame{


    private javax.swing.JPanel main_jPanel;
    private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;

    private javax.swing.JButton jButton_exit;


    public GameRoom() {
        main_jPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        jLabel1.setText("Player1");
        jLabel2.setText("Player2");
        jLabel3.setText("Player3");
        jLabel4.setText("Player4");
        jLabel5.setText("Player5");

        Tetris tetris = new Tetris();
        tetris.init();
        tetris.start();

        Tetris_display tetris2 = new Tetris_display();
        tetris2.init();
        int array[][] = new int[18][10];
        for(int i=0; i<9; i++)
        {
            for(int j=0; j<10; j++)
            {
                array[i][j] = -1;
            }
        }
        for(int i=9; i<18; i++)
        {
            for(int j=0; j<10; j++)
            {
                array[i][j] = 3;
            }
        }
        tetris2.setGrid(array);

        jButton_exit = new javax.swing.JButton();
        jButton_exit.setText("Exit");
        jButton_exit
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        for(int i=0; i<9; i++)
                            {
                                for(int j=0; j<10; j++)
                                {
                                    array[i][j] = 0;
                                }
                            }
                            for(int i=9; i<18; i++)
                            {
                                for(int j=0; j<10; j++)
                                {
                                    array[i][j] = 0;
                                }
                            }
                        update_table(tetris2, array);
                    }
                });

        Tetris_display tetris3 = new Tetris_display();
        tetris3.init();
        Tetris_display tetris4 = new Tetris_display();
        tetris4.init();
        Tetris_display tetris5 = new Tetris_display();
        tetris5.init();


        javax.swing.GroupLayout main_jPanelLayout = new javax.swing.GroupLayout(main_jPanel);
        main_jPanel.setLayout(main_jPanelLayout);
        main_jPanelLayout
                .setVerticalGroup(main_jPanelLayout
                        .createParallelGroup(
                                GroupLayout.Alignment.LEADING)
                        .addGap(18, 18, 18)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(tetris,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                500,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel1)
                                        .addComponent(jButton_exit)
                        )
                        .addGap(18, 18, 18)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(tetris2,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                200,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel2)
                                        .addGap(18, 18, 18)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(tetris4,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                200,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jLabel4)
                                        )
                        )
                        .addGap(18, 18, 18)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(tetris3,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                200,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel3)
                                        .addGap(18, 18, 18)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(tetris5,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                200,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jLabel5)
                                        )

                        )


                );
        main_jPanelLayout
                .setHorizontalGroup(main_jPanelLayout
                        .createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(30, 30, 30)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup()
                                                        .addGap(30, 30, 30)
                                                        .addComponent(tetris,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                250,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jLabel1)
                                                        .addComponent(jButton_exit)
                                        )
                                        .addGap(30, 30, 30)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup()
                                                        .addComponent(tetris2,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel2)
                                                        .addGap(30, 30, 30)
                                                        .addGroup(
                                                                main_jPanelLayout
                                                                        .createParallelGroup()
                                                                        .addComponent(tetris4,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                100,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(jLabel4)
                                                        )
                                        )
                                        .addGap(30, 30, 30)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup()
                                                        .addComponent(tetris3,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel3)
                                                        .addGroup(
                                                                main_jPanelLayout
                                                                        .createParallelGroup()
                                                                        .addComponent(tetris5,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                100,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(jLabel5)
                                                        )
                                        )


                                        .addContainerGap()
                        )
                );

        setBounds(0, javax.swing.GroupLayout.PREFERRED_SIZE, 1000, 700);

        this.setTitle("Waiting Room");
        this.add(main_jPanel);
        setResizable(false);// 창 크기 못바꾸게

    }


    public void update_table(Tetris_display tetris, int[][] array) {
        tetris.init();
        tetris.setGrid(array);
    }
}
