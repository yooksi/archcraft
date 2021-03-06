package yooksi.betterarchery.item;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yooksi.betterarchery.common.BetterArchery;
import yooksi.betterarchery.init.ModItems;
import yooksi.betterarchery.item.ItemBowPartBody.BodyPartType;

/** 
 * An abstract parent class for all custom bows in this mod. <br>
 * Every bow should extend this class and be initialized with {@link #initNewBowType}.
 */
public abstract class ArchersBow extends ItemBow
{	
	/** This multiplier modifies the duration of the bows pulling animation. */
	private final float pullingSpeedMult;

	/** This multiplier modifies the speed and damage of an arrow being shot from this bow. */
	private final float arrowSpeedMult;

	private final BowItemVariant variant;

	private static final java.util.Map<BodyPartType, ArchersBow> craftingContracts = 
			new java.util.HashMap<BodyPartType, ArchersBow>();

	protected ArchersBow(BowItemVariant variant, float pullingMult, float arrowMult)
	{
		craftingContracts.put(variant.bodyType, this);
		this.variant = variant;

		this.pullingSpeedMult = pullingMult > 0.0F ? pullingMult : 1.0F;
		this.arrowSpeedMult = arrowMult > 0.0F ? arrowMult : 1.0F;
	}

	/**
	 *  Get an item bow that is a crafting product of this body type.
	 *  @param type should have a parent <code>BowItemVariant</code>, otherwise return will be <code>null</code>.
	 */
	@Nullable
	public static ArchersBow getCraftingOutputFor(BodyPartType type)
	{
		return craftingContracts.get(type);
	}

	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, java.util.List<ItemStack> subItems)
	{
		/*
		 *  This method here is only used to create stack NBT when in creative mode.
		 *  When crafting the NBT is being created in EventHandler.
		 */
		ItemStack archersBow = new ItemStack(itemIn);

		NBTTagCompound nbt = new NBTTagCompound();
		archersBow.setTagCompound(nbt);

		subItems.add(archersBow);
	}

	public static BowItemVariant getBowItemVariant(Item item)
	{
		return item instanceof ArchersBow ? ((ArchersBow) item).variant : null;
	}

	public enum BowItemVariant
	{
		SIMPLE_BOW_PLAIN("simple_bow", BodyPartType.TYPE_BODY_SIMPLE_PLAIN), 
		RECURVE_BOW_PLAIN("recurve_bow", BodyPartType.TYPE_BODY_RECURVE_PLAIN),
		LONGBOW_PLAIN("longbow", BodyPartType.TYPE_BODY_LONG_PLAIN),

		SIMPLE_BOW_WOOLEN_GRIP ("simple_bow", SIMPLE_BOW_PLAIN, BodyPartType.TYPE_BODY_SIMPLE_WITH_WOOLEN_GRIP),
		SIMPLE_BOW_LEATHER_GRIP ("simple_bow", SIMPLE_BOW_PLAIN, BodyPartType.TYPE_BODY_SIMPLE_WITH_LEATHER_GRIP),

		RECURVE_BOW_WOOLEN_GRIP ("recurve_bow", RECURVE_BOW_PLAIN, BodyPartType.TYPE_BODY_RECURVE_WITH_WOOLEN_GRIP),
		RECURVE_BOW_LEATHER_GRIP ("recurve_bow", RECURVE_BOW_PLAIN, BodyPartType.TYPE_BODY_RECURVE_WITH_LEATHER_GRIP),

		LONGBOW_WOOLEN_GRIP ("longbow", LONGBOW_PLAIN, BodyPartType.TYPE_BODY_LONG_WITH_WOOLEN_GRIP),
		LONGBOW_LEATHER_GRIP ("longbow", LONGBOW_PLAIN, BodyPartType.TYPE_BODY_LONG_WITH_LEATHER_GRIP);

		private final BowItemVariant parent;
		private final String modelFileName;
		public final BodyPartType bodyType;

		BowItemVariant(String modelFile, BodyPartType bodyType)   // parents constructor
		{
			this(modelFile, null, bodyType);
		}

		BowItemVariant(String modelFile, BowItemVariant parent, BodyPartType bodyType)
		{
			this.parent = parent;
			this.modelFileName = modelFile;
			this.bodyType = bodyType;
		}

		/**
		 *  Returns a decimal color value <i>(accepted by Minecraft)</i> of the variant, 
		 *  or <b>-1</b> if no color.  
		 */
		protected int getColorRGB()
		{
			return bodyType.getColorRGB();
		}

		/**
		 *  Get the location of the model file used by this variant.
		 *  @param pseudo generate a non-existing location for parent variants. 
		 */
		public ModelResourceLocation getModelResourceLocation(boolean pseudo)
		{
			String fileName = (pseudo && parent == null ? modelFileName + "_pseudo" : modelFileName);
			return new ModelResourceLocation(BetterArchery.MODID + ":" + fileName);
		}

		/**
		 *  Compile and return a list of all variants that are considered parents.
		 */
		public static java.util.List<BowItemVariant> getParents()
		{
			// TODO: Consider declaring this list as a static element if we 
			//       start calling this method more often.

			java.util.List<BowItemVariant> list = new java.util.ArrayList<BowItemVariant>();
			for (BowItemVariant variant : BowItemVariant.values())
			{
				if (variant.parent == null)
					list.add(variant);
			}

			return list;
		}
	}		

	/** 
	 *  Model file names for each bow variant have been stored in <i>BowItemVariant</i>. <br>
	 *  These names are needed by <i>ClientProxy</i> to register our model files with <i>ModelLoader</i>. <p> 
	 *
	 *  For convenience, the construction of this object has been placed here, so we don't have to <br>
	 *  repeat the same lines of code for every item variant.
	 */
	public ModelResourceLocation getModelResourceLocation()
	{
		return variant.getModelResourceLocation(true);
	}

	/** 
	 * Perform custom bow initialization after it's been created. <br>  
	 * This initialization protocol should be followed by all custom bows.
	 * 
	 * @param <T> has to be a valid custom bow type
	 * @param customBow newly created instance of the custom bow
	 */
	public static <T extends ArchersBow> T initNewBowType(T customBow)
	{
		addPropertyOverrides(customBow);
		customBow.setCreativeTab(BetterArchery.creativeTab);
		return customBow;
	}

	/** 
	 * This is what enables the bow to play the pulling animation.
	 * 
	 * @param <T> has to be a valid custom bow type
	 * @param item your custom bow instance <i>(not cast)</i>.  
	 */
	protected static <T extends ArchersBow> void addPropertyOverrides(final T item)
	{
		item.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter()
		{
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
			{
				if (entityIn != null)
				{
					/* Change the speed of the pulling animation here. The animation is divided into three stages,
					 * and the speed of every stage is exponentially increased as they progress.
					 * That's why we use a multiplier, instead of directly increasing or decreasing the value.
					 */
					ItemStack itemstack = entityIn.getActiveItemStack();
					return itemstack != null && itemstack.getItem() == item ? item.getPullingAnimationProgress(stack, entityIn) : 0.0F;
				}
				else return 0.0F;
			}
		});
		item.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter()
		{
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
			{
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
			}
		});
	}

	/**
	 * Try to find ammunition this bow can fire in player's possesion. <br>
	 * First check <b>OFF_HAND</b> and <b>MAIN_HAND</b> then go through the inventory.
	 * 
	 * @return ItemStack of the ammo or <code>null</code> if no ammo was found
	 */
	private ItemStack findAmmo(EntityPlayer player)   
	{
		if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND)))
		{
			return player.getHeldItem(EnumHand.OFF_HAND);
		}
		else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND)))
		{
			return player.getHeldItem(EnumHand.MAIN_HAND);
		}
		else
		{
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
			{
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (this.isArrow(itemstack))
					return itemstack;
			}

			return null;
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
	{
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer)entityLiving;
			boolean flag = entityplayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
			ItemStack itemstack = this.findAmmo(entityplayer);

			int i = this.getMaxItemUseDuration(stack) - timeLeft;
			i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, (EntityPlayer)entityLiving, i, itemstack != null || flag);
			if (i < 0) return;

			if (itemstack != null || flag)
			{
				if (itemstack == null)
				{
					itemstack = new ItemStack(Items.ARROW);
				}

				/*  Vanilla arrow velocity value ranges from 0.0F to 1.0F.
				 *  Increasing arrow velocity will proportionally increase it's damage,
				 *  as well as the pitch of the sound played upon releasing the arrow.
				 */
				float f = (float)i / 20.0F * pullingSpeedMult;
				f = f < 1.0F ? (f * f + f * 2.0F) / 3.0F * arrowSpeedMult : arrowSpeedMult;

				if ((double)f >= 0.1D * arrowSpeedMult)
				{
					boolean itemStackInfinite = entityplayer.capabilities.isCreativeMode || (itemstack.getItem() instanceof ItemArrow ? ((ItemArrow)itemstack.getItem()).isInfinite(itemstack, stack, entityplayer) : false);

					if (!worldIn.isRemote)
					{
						ItemArrow itemarrow = (ItemArrow)((ItemArrow)(itemstack.getItem() instanceof ItemArrow ? itemstack.getItem() : Items.ARROW));
						EntityArrow entityarrow = itemarrow.createArrow(worldIn, itemstack, entityplayer);
						entityarrow.setAim(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F, f * 3.0F, 1.0F);

						if (f >= 1.0F * arrowSpeedMult)
							entityarrow.setIsCritical(true);

						int enchLvlPower = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
						int enchLvlPunch = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
						int enchLvlFlame  = EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack);

						if (enchLvlPower > 0)
							entityarrow.setDamage(entityarrow.getDamage() + (double)enchLvlPower * 0.5D + 0.5D);

						if (enchLvlPunch > 0)
							entityarrow.setKnockbackStrength(enchLvlPunch);

						if (enchLvlFlame > 0)
							entityarrow.setFire(100);

						if (!entityplayer.capabilities.isCreativeMode)
						{
							final int damage = worldIn.getDifficulty() == EnumDifficulty.HARD && entityarrow.getIsCritical() ? 2 : 1;

							stack.damageItem(damage, entityplayer);

							final int bowStringDamage = stack.getTagCompound().getInteger("bow_string_damage") + damage;
							boolean isBowStringBroken = bowStringDamage > ModItems.BOW_STRING_ITEM.getMaxDamage();

							float randomFloat = worldIn.rand.nextFloat();
							float durabilityMod = (float)bowStringDamage / (float)ModItems.BOW_STRING_ITEM.getMaxDamage() + 1.0F;

							if (stack.stackSize == 0)  // Bow has been broken after dealing durability damage
							{
								if (!isBowStringBroken)
								{
									ItemStack bowString = new ItemStack(ModItems.BOW_STRING_ITEM);
									bowString.setItemDamage(bowStringDamage);

									entityplayer.inventory.addItemStackToInventory(bowString);
								}
							}
							else if (isBowStringBroken || randomFloat < 0.0028F * durabilityMod)  // Bow string just broke
							{
								ItemStack bowBody = new ItemStack(ModItems.BOW_ITEM_PART_BODY, 1, variant.bodyType.getTypeMetadata()); 

								NBTTagCompound tagCompound = new NBTTagCompound();
								bowBody.setTagCompound(tagCompound);

								tagCompound.setInteger("item_damage", 80);

								if (stack.getTagCompound().hasKey("dyeColorMeta"))
									tagCompound.setInteger("dyeColorMeta", stack.getTagCompound().getInteger("dyeColorMeta"));

								entityplayer.setHeldItem(entityplayer.getActiveHand(), bowBody);
							}
							else stack.getTagCompound().setInteger("bow_string_damage", bowStringDamage);
						}

						if (itemStackInfinite)
							entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;

						worldIn.spawnEntityInWorld(entityarrow);
					}

					final float soundPitch = 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F;
					worldIn.playSound((EntityPlayer)null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, soundPitch);

					if (!itemStackInfinite && --itemstack.stackSize == 0)  
						entityplayer.inventory.deleteStack(itemstack);

					entityplayer.addStat(StatList.getObjectUseStats(this));
				}
			}
		}
	}

	/**
	 * Get the current progress of the bow's pulling animation for entity. <br>
	 * <i>When pulling is in progress, the return will increase by <b>0.05</b> every sixth tick.</i> <p>
	 * 
	 * The progress can be divided into three stages; check the method comments for more info. <br>
	 * These stages and associated values are important for updating entity FOV.
	 * 
	 * @return a value between 0 <i>(did not start pulling)</i> and 1 <i>(fully pulled)</i>.  
	 */
	public float getPullingAnimationProgress(ItemStack bow, EntityLivingBase archer)
	{
		/*
		 * pulling_0: 0.00 - 0.60 - Slightly pulled
		 * pulling_1: 0.65 - 0.85 - Moderately pulled
		 * pulling_2: 0.90 - 1.00 - Fully pulled
		 */

		float animationProgress = (bow != null && archer != null) ? (float)(bow.getMaxItemUseDuration() - archer.getItemInUseCount()) / 20.0F : 0.0F;
		return (float)(animationProgress *= pullingSpeedMult) > 1.0F ? 1.0F : animationProgress;
	}
}