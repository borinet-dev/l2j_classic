/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.model.html.pagehandlers;

import org.l2jmobius.gameserver.model.html.IBypassFormatter;
import org.l2jmobius.gameserver.model.html.IHtmlStyle;
import org.l2jmobius.gameserver.model.html.IPageHandler;

/**
 * Creates pager with links << | < | > | >>
 * @author UnAfraid
 */
public class NextPrevPageHandler implements IPageHandler
{
	public static final NextPrevPageHandler INSTANCE = new NextPrevPageHandler();
	
	@Override
	public void apply(String bypass, int currentPage, int pages, StringBuilder sb, IBypassFormatter bypassFormatter, IHtmlStyle style)
	{
		// Beginning
		// sb.append(style.applyBypass(bypassFormatter.formatBypass(bypass, 0), "<<", (currentPage - 1) < 0));
		
		// Separator
		// sb.append(style.applySeparator());
		
		// Previous
		if (currentPage >= 1)
		{
			sb.append(style.applyBypass(bypassFormatter.formatBypass(bypass, currentPage - 1), "이전", currentPage <= 0));
		}
		// sb.append(style.applySeparator());
		// sb.append(String.format("<td align=\"center\">%d/%d</td>", currentPage + 1, pages + 1));
		// sb.append(style.applySeparator());
		
		// Next
		if (currentPage < (pages - 1))
		{
			sb.append(style.applyBypass(bypassFormatter.formatBypass(bypass, currentPage + 1), "다음", currentPage >= pages));
		}
		
		// Separator
		// sb.append(style.applySeparator());
		
		// End
		// sb.append(style.applyBypass(bypassFormatter.formatBypass(bypass, pages), ">>", (currentPage + 1) > pages));
	}
}