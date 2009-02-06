////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2003-2006 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package mx.managers.systemClasses
{

[ExcludeClass]

import flash.display.Stage;
import flash.events.Event;

/**
 * An object that filters stage
 */
public class StageEventProxy
{
	private var listener:Function;

	public function StageEventProxy(listener:Function)
	{
		this.listener = listener;
	}

	public function stageListener(event:Event):void
	{
		if (event.target is Stage)
			listener(event);
	}

}

}