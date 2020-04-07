package cn.olange.pins.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MyVFlowLayout extends FlowLayout {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Specify alignment top.
	 */
	public static final int TOP = 0;

	/**
	 * Specify a middle alignment.
	 */
	public static final int MIDDLE = 1;

	/**
	 * Specify the alignment to be bottom.
	 */
	public static final int BOTTOM = 2;

	/**
	 * Specify the alignment to be left.
	 */
	public static final int LEFT = 0;

	/**
	 * Specify the alignment to be right.
	 */
	public static final int RIGHT = 2;

	private int horizontalAlignment;
	private int topVerticalGap;
	private int bottomVerticalGap;
	private boolean isHorizontalFill;
	private boolean isVerticalFill;

	public MyVFlowLayout()
	{
		this(TOP, MIDDLE, 5, 5, 5, 5, true, false);
	}

	public MyVFlowLayout(boolean isHorizontalFill, boolean isVerticalFill)
	{
		this(TOP, MIDDLE, 5, 5, 5, 5, isHorizontalFill, isVerticalFill);
	}

	public MyVFlowLayout(int align)
	{
		this(align, MIDDLE, 5, 5, 5, 5, true, false);
	}

	public MyVFlowLayout(int align, boolean isHorizontalFill, boolean isVerticalFill)
	{
		this(align, MIDDLE, 5, 5, 5, 5, isHorizontalFill, isVerticalFill);
	}

	public MyVFlowLayout(int align, int horizontalGap, int verticalGap, boolean isHorizontalFill, boolean isVerticalFill)
	{
		this(align, MIDDLE, horizontalGap, verticalGap, verticalGap, verticalGap, isHorizontalFill, isVerticalFill);
	}

	public MyVFlowLayout(int align, int horizontalGap, int verticalGap, int topVerticalGap, int bottomVerticalGap, boolean isHorizontalFill, boolean isVerticalFill)
	{
		this(align, MIDDLE, horizontalGap, verticalGap, topVerticalGap, bottomVerticalGap, isHorizontalFill, isVerticalFill);
	}

	/**
	 * Construct a new MyVFlowLayout.
	 *
	 * @param verticalAlignment
	 *            the alignment value
	 * @param horizontalAlignment
	 *            the horizontal alignment value
	 * @param horizontalGap
	 *            the horizontal gap variable
	 * @param verticalGap
	 *            the vertical gap variable
	 * @param topVerticalGap
	 *            the top vertical gap variable
	 * @param bottomVerticalGap
	 *            the bottom vertical gap variable
	 * @param isHorizontalFill
	 *            the fill to edge flag
	 * @param isVerticalFill
	 *            true if the panel should vertically fill.
	 */
	public MyVFlowLayout(int verticalAlignment, int horizontalAlignment, int horizontalGap, int verticalGap, int topVerticalGap, int bottomVerticalGap, boolean isHorizontalFill, boolean isVerticalFill)
	{
		this.setAlignment(verticalAlignment);
		this.setHorizontalAlignment(horizontalAlignment);
		this.setHgap(horizontalGap);
		this.setVgap(verticalGap);
		this.setTopVerticalGap(topVerticalGap);
		this.setBottomVerticalGap(bottomVerticalGap);
		this.setHorizontalFill(isHorizontalFill);
		this.setVerticalFill(isVerticalFill);
	}

	public void setHorizontalAlignment(int horizontalAlignment)
	{
		if(LEFT == horizontalAlignment)
		{
			this.horizontalAlignment = LEFT;
		}
		else if(RIGHT == horizontalAlignment)
		{
			this.horizontalAlignment = RIGHT;
		}
		else
		{
			this.horizontalAlignment = MIDDLE;
		}
	}

	public int getHorizontalAlignment()
	{
		return this.horizontalAlignment;
	}

	@Override
	public void setHgap(int horizontalGap)
	{
		super.setHgap(horizontalGap);
	}

	@Override
	public void setVgap(int verticalGap)
	{
		super.setVgap(verticalGap);
	}

	public void setTopVerticalGap(int topVerticalGap)
	{
		this.topVerticalGap = topVerticalGap;
	}

	public int getTopVerticalGap()
	{
		return this.topVerticalGap;
	}

	public void setBottomVerticalGap(int bottomVerticalGap)
	{
		this.bottomVerticalGap = bottomVerticalGap;
	}

	public int getBottomVerticalGap()
	{
		return this.bottomVerticalGap;
	}

	/**
	 * Set true to fill vertically.
	 *
	 * @param isVerticalFill
	 *            true to fill vertically.
	 */
	public void setVerticalFill(boolean isVerticalFill)
	{
		this.isVerticalFill = isVerticalFill;
	}

	/**
	 * Returns true if the layout vertically fills.
	 *
	 * @return true if vertically fills the layout using the specified.
	 */
	public boolean getVerticalFill()
	{
		return isVerticalFill;
	}

	/**
	 * Set to true to enable horizontally fill.
	 *
	 *            true to fill horizontally.
	 */
	public void setHorizontalFill(boolean isHorizontalFill)
	{
		this.isHorizontalFill = isHorizontalFill;
	}

	/**
	 * Returns true if the layout horizontally fills.
	 *
	 * @return true if horizontally fills.
	 */
	public boolean getHorizontalFill()
	{
		return isHorizontalFill;
	}

	/**
	 * Returns the preferred dimensions given the components in the target
	 * container.
	 *
	 * @param container
	 *            the component to lay out
	 */
	@Override
	public Dimension preferredLayoutSize(Container container)
	{
		Dimension rs = new Dimension(0, 0);
		List<Component> components = this.getVisibleComponents(container);
		Dimension dimension = this.preferredComponentsSize(components);
		rs.width += dimension.width;
		rs.height += dimension.height;
		Insets insets = container.getInsets();
		rs.width += insets.left + insets.right;
		rs.height += insets.top + insets.bottom;

		if(0 < components.size())
		{
			rs.width += this.getHgap() * 2;
			rs.height += this.topVerticalGap;
			rs.height += this.bottomVerticalGap;
		}

		return rs;
	}

	/**
	 * Returns the minimum size needed to layout the target container.
	 *
	 * @param container
	 *            the component to lay out.
	 * @return the minimum layout dimension.
	 */
	@Override
	public Dimension minimumLayoutSize(Container container)
	{
		Dimension rs = new Dimension(0, 0);
		List<Component> components = this.getVisibleComponents(container);
		Dimension dimension = this.minimumComponentsSize(components);
		rs.width += dimension.width;
		rs.height += dimension.height;
		Insets insets = container.getInsets();
		rs.width += insets.left + insets.right;
		rs.height += insets.top + insets.bottom;

		if(0 < components.size())
		{
			rs.width += this.getHgap() * 2;
			rs.height += this.topVerticalGap;
			rs.height += this.bottomVerticalGap;
		}

		return rs;
	}

	@Override
	public void layoutContainer(Container container)
	{
		int horizontalGap = this.getHgap();
		int verticalGap = this.getVgap();
		Insets insets = container.getInsets();
		int maxWidth = container.getSize().width - (insets.left + insets.right + horizontalGap * 2);
		int maxHeight = container.getSize().height - (insets.top + insets.bottom + this.topVerticalGap + this.bottomVerticalGap);
		List<Component> components = this.getVisibleComponents(container);
		Dimension preferredComponentsSize = this.preferredComponentsSize(components);
		int alignment = this.getAlignment();
		int y = insets.top + this.topVerticalGap;;

		if(!this.isVerticalFill && preferredComponentsSize.height < maxHeight)
		{
			if(MIDDLE == alignment)
			{
				y += (maxHeight - preferredComponentsSize.height) / 2;
			}
			else if(BOTTOM == alignment)
			{
				y += maxHeight - preferredComponentsSize.height;
			}
		}

		int index = 0;

		for(Component component : components)
		{
			int x = insets.left + horizontalGap;
			Dimension dimension = component.getPreferredSize();

			if(this.isHorizontalFill)
			{
				dimension.width = maxWidth;
			}
			else
			{
				dimension.width = Math.min(maxWidth, dimension.width);

				if(MIDDLE == this.horizontalAlignment)
				{
					x += (maxWidth - dimension.width) / 2;
				}
				else if(RIGHT == this.horizontalAlignment)
				{
					x += maxWidth - dimension.width;
				}
			}

			if(this.isVerticalFill && index == components.size() - 1)
			{
				int height = maxHeight + this.topVerticalGap + insets.top - y;
				dimension.height = Math.max(height, dimension.height);
			}

			component.setSize(dimension);
			component.setLocation(x, y);
			y += dimension.height + verticalGap;
			index++;
		}
	}

	private Dimension preferredComponentsSize(List<Component> components)
	{
		Dimension rs = new Dimension(0, 0);

		for(Component component : components)
		{
			Dimension dimension = component.getPreferredSize();
			rs.width = Math.max(rs.width, dimension.width);
			rs.height += dimension.height;
		}

		if(0 < components.size())
		{
			rs.height += this.getVgap() * (components.size() - 1);
		}

		return rs;
	}

	private Dimension minimumComponentsSize(List<Component> components)
	{
		Dimension rs = new Dimension(0, 0);

		for(Component component : components)
		{
			Dimension dimension = component.getMinimumSize();
			rs.width = Math.max(rs.width, dimension.width);
			rs.height += dimension.height;
		}

		if(0 < components.size())
		{
			rs.height += this.getVgap() * (components.size() - 1);
		}

		return rs;
	}

	private List<Component> getVisibleComponents(Container container)
	{
		List<Component> rs = new ArrayList<Component>();

		for(Component component : container.getComponents())
		{
			if(component.isVisible())
			{
				rs.add(component);
			}
		}

		return rs;
	}

}
