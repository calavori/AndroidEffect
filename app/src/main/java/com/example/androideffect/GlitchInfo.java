package com.example.androideffect;

public class GlitchInfo {
        private int seed;
        private int rbg_shift;
        private int thickness;

    public GlitchInfo(){
            this.seed = 0;
            this.rbg_shift = 0;
            this.thickness = 0;
        }

    public GlitchInfo(int seed, int rbg_shift, int thickness){
            this.seed=seed;
            this.rbg_shift=rbg_shift;
            this.thickness=thickness;
        }

    public void setSeed(int seed) {
        this.seed = seed;
    }
    public void setRbg_shift(int rbg_shift){
        this.rbg_shift = rbg_shift;
    }
    public void  setThickness(int thickness){
        this.thickness = thickness;
    }
    public int getSeed(){
        return this.seed;
    }
    public int getRbg_shift(){
        return this.rbg_shift;
    }
    public int getThickness(){
        return this.thickness;
    }
}
