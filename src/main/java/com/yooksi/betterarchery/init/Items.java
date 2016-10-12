package com.yooksi.betterarchery.init;

import com.yooksi.betterarchery.common.BetterArchery;
import com.yooksi.betterarchery.item.*;

/** All custom items are initialized and stored here. */
public class Items 
{
	public static final SimpleBow SIMPLE_BOW;
	public static final RecurveBow RECURVE_BOW;
	
	public static final SimpleBowLimb SIMPLE_BOW_LIMB;

	static
	{
		SIMPLE_BOW = ArchersBow.initNewBowType(new SimpleBow());
		RECURVE_BOW = ArchersBow.initNewBowType(new RecurveBow());
		SIMPLE_BOW_LIMB = (SimpleBowLimb) new SimpleBowLimb().setCreativeTab(BetterArchery.tabBetterArchery);
	}
}