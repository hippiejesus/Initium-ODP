package com.universeprojects.miniup.server.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class ExperimentalPageUpdateService extends MainPageUpdateService
{

	public ExperimentalPageUpdateService(ODPDBAccess db, CachedEntity user, CachedEntity character, CachedEntity location, OperationBase operation)
	{
		super(db, user, character, location, operation);
	}

	@Override
	protected String updateButtonList_CombatMode()
	{
		return "";
	}

	@Override
	protected String updateButtonList_Incapacitated()
	{
		return "";
	}
	
	@Override
	public String updatePartyView()
	{
		StringBuilder newHtml = new StringBuilder();

		if (isInParty())
		{
			newHtml.append("<div class='v3-party-panel-contents'>");
			List<CachedEntity> party = getParty();
			if (party!=null)
			{
				String mode = (String)character.getProperty("mode");
				Boolean isCharacterBusy = (mode!=null && mode.equals("NORMAL")==false);

				// Get Characters and Users
				EntityPool pool = new EntityPool(db.getDB());
				for(CachedEntity partyCharacter:party)
				{
					pool.addToQueue((Key)partyCharacter.getProperty("userKey"));
				}
				Collections.sort(party, new Comparator<CachedEntity>(){
					@Override
					public int compare(CachedEntity c1, CachedEntity c2){
						String partyCharacter1 = (String)c1.getProperty("name");
						String partyCharacter2 = (String)c2.getProperty("name");
						return partyCharacter1.compareTo(partyCharacter2);
					}
				});
				pool.loadEntities();

				for(CachedEntity partyCharacter:party)
				{
					if (GameUtils.equals(partyCharacter.getKey(), character.getKey()))
						continue;
					
					CachedEntity partyUser = pool.get((Key)partyCharacter.getProperty("userKey"));
					boolean isThisMemberTheLeader = false;
					if ("TRUE".equals(partyCharacter.getProperty("partyLeader")))
						isThisMemberTheLeader = true;
					boolean dead = CommonChecks.checkCharacterIsUnconscious(partyCharacter);

					newHtml.append("<br>");
					newHtml.append("<div style='display:inline-block;vertical-align:top;'>");
					newHtml.append("<a class='main-item clue' style='width:inherit;' rel='/odp/viewcharactermini?characterId="+partyCharacter.getKey().getId()+"'>");
					newHtml.append("<div style='display:inline-block; position:relative;'>");
					newHtml.append(GameUtils.renderCharacterWidget(db.getRequest(), db, partyCharacter, partyUser, true));
					newHtml.append("<div style='position:absolute; top:0px; bottom: 0px; left:55px; right: 0px;'></div>");// Cover the widget so we can't click it
					newHtml.append("</div>");
					newHtml.append("<br>");
					if (!GameUtils.equals(character.getKey(), partyCharacter.getKey()) && user!=null && 
							GameUtils.equals(user.getKey(), partyUser.getKey())) {
						newHtml.append("<div class='main-item-controls' style='top:0px'>");
						newHtml.append("<a onclick='switchCharacter(event, "+partyCharacter.getKey().getId()+")'>Switch</a>");
						newHtml.append("</div>");
					}

					if (isThisMemberTheLeader)
						newHtml.append("<div class='main-item-controls' style='top:0px;'>(Leader)</div>");
					else if (dead)
					{
						if(GameUtils.equals(character.getProperty("locationKey"), partyCharacter.getProperty("locationKey")))
						{
							newHtml.append("<div class='main-item-controls' style='top:0px'>");
							newHtml.append("<a onclick='collectDogecoinFromCharacter("+partyCharacter.getKey().getId()+")'>Collect "+partyCharacter.getProperty("dogecoins")+" gold</a>");
							newHtml.append("</div>");
						}
					}
					else if (!isCharacterBusy)
					{
						newHtml.append("<div class='main-item-controls' style='top:0px'>");
						// If this party character is not currently the leader and we are the current party leader then render the "make leader" button
						if (isThisMemberTheLeader == false && isPartyLeader())
							newHtml.append("<a onclick='doSetLeader(event, " + partyCharacter.getKey().getId() + ", \"" + partyCharacter.getProperty("name") + "\")'>Make Leader</a>");
						newHtml.append("</div>");
					}
					newHtml.append("</a>");
					newHtml.append("</div>");
				}
			}
			
			newHtml.append("<p><a onclick='doLeaveParty(event)'>Leave Party</a></p>");
			
			newHtml.append("</div>");
		}
		
		return updateHtmlContents("#partyPanel", newHtml.toString());
	}
	
	@Override
	protected String getSideBannerLinks()
	{
		StringBuilder newHtml = new StringBuilder();

//		newHtml.append("<div class='path-overlay-link major-banner-links' style='left:50%; top:15%;'>");
//		newHtml.append("<a id='thisLocation-button' class='button-overlay-major' onclick='makeIntoPopup(\".this-location-box\")' style='right:0px;top:0px;'><img alt='Location actions' src='https://initium-resources.appspot.com/images/ui/magnifying-glass2.png'></a>");			
//		newHtml.append("<a id='globe-navigation-button' class='button-overlay-major' onclick='viewGlobeNavigation()' style='right:4px;top:74px;'><img alt='Global navigation' src='https://initium-resources.appspot.com/images/ui/navigation-map-icon2.png'></a>");			
//		newHtml.append("<a id='local-navigation-button' class='button-overlay-major' onclick='viewLocalNavigation()' style='right:4px;top:108px;'><img alt='Local navigation' src='https://initium-resources.appspot.com/images/ui/navigation-local-icon1.png'></a>");			
//		newHtml.append("<a id='guard-button' class='button-overlay-major' onclick='viewGuardSettings()' style='right:4px;top:142px;'><img alt='Guard settings' src='https://initium-resources.appspot.com/images/ui/guardsettings1.png'></a>");
//		newHtml.append("</div>");
		
		return newHtml.toString();
	}

	public String updateLocation2D()
	{
		StringBuilder newHtml = new StringBuilder();
		
		newHtml.append("<div id='viewportcontainer' class='vpcontainer'>");
		newHtml.append("<div id='menu' class='menuContainer' style='visibility: hidden;'></div>");
		newHtml.append("<div id='viewport' class='vp'>");
		newHtml.append("<div id='grid' class='grid'>");
		newHtml.append("<div id='ui-layer' class='uiLayer'></div>");
		newHtml.append("<div id='cell-layer' class='cellLayer'></div>");
		newHtml.append("<div id='ground-layer' class='groundLayer'></div>");
		newHtml.append("<div id='object-layer' class='objectLayer'></div>");
		newHtml.append("</div>");
		newHtml.append("</div>");
		newHtml.append("</div>");
		newHtml.append("<button type='button' onclick='openMenu()'>Menu</button>");
		newHtml.append("<button type='button' onclick='mapPlow(event)'>Plow</button>");
		newHtml.append("<button type='button' onclick='mapPlaceHouse(event)' style='position:relative'>Place House</button>");
		newHtml.append("<button type='button' onclick='mapPlaceCity(event)' style='position:relative'>Place City</button>");
		newHtml.append("<center><p id='selectedObjects' class='selectedObjectList'></p></center>");
		newHtml.append("<script type='text/javascript' src='/odp/javascript/Sandbox.js?v="+GameUtils.version+"'></script>");
		newHtml.append("<script>");
		newHtml.append("var mapData = '" + GridMapService.buildNewGrid(123456,20,20,2).toString() + "';");
		newHtml.append("$(document).on('click', '#somebutton', function() { pressedButton(); });");
		newHtml.append("</script>");
		
		return updateHtmlContents(".location-2d", newHtml.toString());
	}
	
	
}
